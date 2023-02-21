/*
 * Copyright (c) 2018, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.graal.python.nodes.argument.keywords;

import static com.oracle.graal.python.builtins.PythonBuiltinClassType.TypeError;

import com.oracle.graal.python.builtins.objects.common.EmptyStorage;
import com.oracle.graal.python.builtins.objects.common.HashingStorage;
import com.oracle.graal.python.builtins.objects.common.HashingStorageNodes;
import com.oracle.graal.python.builtins.objects.common.HashingStorageNodes.HashingStorageIterator;
import com.oracle.graal.python.builtins.objects.common.HashingStorageNodes.HashingStorageLen;
import com.oracle.graal.python.builtins.objects.common.KeywordsStorage;
import com.oracle.graal.python.builtins.objects.dict.PDict;
import com.oracle.graal.python.builtins.objects.function.BuiltinMethodDescriptors;
import com.oracle.graal.python.builtins.objects.function.PKeyword;
import com.oracle.graal.python.nodes.ErrorMessages;
import com.oracle.graal.python.nodes.PGuards;
import com.oracle.graal.python.nodes.PNodeWithContext;
import com.oracle.graal.python.nodes.PRaiseNode;
import com.oracle.graal.python.nodes.attributes.LookupCallableSlotInMRONode;
import com.oracle.graal.python.nodes.object.GetClassNode;
import com.oracle.graal.python.nodes.util.CannotCastException;
import com.oracle.graal.python.nodes.util.CastToTruffleStringNode;
import com.oracle.graal.python.runtime.PythonOptions;
import com.oracle.truffle.api.CompilerDirectives.ValueType;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;

@GenerateUncached
public abstract class MappingToKeywordsNode extends PNodeWithContext {

    public abstract PKeyword[] execute(VirtualFrame frame, Object starargs) throws SameDictKeyException, NonMappingException;

    @Specialization(guards = "hasBuiltinIter(starargs, getClassNode, lookupIter)", limit = "1")
    static PKeyword[] doDict(PDict starargs,
                    @SuppressWarnings("unused") @Cached GetClassNode getClassNode,
                    @SuppressWarnings("unused") @Cached(parameters = "Iter") LookupCallableSlotInMRONode lookupIter,
                    @Shared("convert") @Cached HashingStorageToKeywords convert) {
        return convert.execute(starargs.getDictStorage());
    }

    @Fallback
    static PKeyword[] doMapping(VirtualFrame frame, Object starargs,
                    @Cached ConcatDictToStorageNode concatDictToStorageNode,
                    @Shared("convert") @Cached HashingStorageToKeywords convert) throws SameDictKeyException, NonMappingException {
        HashingStorage storage = concatDictToStorageNode.execute(frame, EmptyStorage.INSTANCE, starargs);
        return convert.execute(storage);
    }

    /* CPython tests that tp_iter is dict_iter */
    protected static boolean hasBuiltinIter(PDict dict, GetClassNode getClassNode, LookupCallableSlotInMRONode lookupIter) {
        return PGuards.isBuiltinDict(dict) || lookupIter.execute(getClassNode.execute(dict)) == BuiltinMethodDescriptors.DICT_ITER;
    }

    @GenerateUncached
    @ImportStatic(PythonOptions.class)
    abstract static class AddKeywordNode extends HashingStorageNodes.HashingStorageForEachCallback<CopyKeywordsState> {
        @Override
        public abstract CopyKeywordsState execute(Frame frame, Node inliningTarget, HashingStorage storage, HashingStorageIterator it, CopyKeywordsState accumulator);

        @Specialization
        public CopyKeywordsState add(@SuppressWarnings("unused") Node inliningTarget, HashingStorage storage, HashingStorageNodes.HashingStorageIterator it, CopyKeywordsState state,
                        @Cached PRaiseNode raiseNode,
                        @Cached CastToTruffleStringNode castToTruffleStringNode,
                        @Cached HashingStorageNodes.HashingStorageIteratorKey itKey,
                        @Cached HashingStorageNodes.HashingStorageIteratorKeyHash itKeyHash,
                        @Cached HashingStorageNodes.HashingStorageGetItemWithHash getItem) {
            Object key = itKey.execute(storage, it);
            long hash = itKeyHash.execute(storage, it);
            Object value = getItem.execute(null, storage, key, hash);
            try {
                state.addKeyword(castToTruffleStringNode.execute(key), value);
            } catch (CannotCastException e) {
                throw raiseNode.raise(TypeError, ErrorMessages.KEYWORDS_S_MUST_BE_STRINGS);
            }
            return state;
        }
    }

    @ValueType
    protected static final class CopyKeywordsState {
        private final PKeyword[] keywords;
        private int i = 0;

        public CopyKeywordsState(PKeyword[] keywords) {
            this.keywords = keywords;
        }

        void addKeyword(TruffleString key, Object value) {
            assert i < keywords.length : "AddKeywordNode: current index (over hashingStorage) exceeds keywords array length!";
            keywords[i++] = new PKeyword(key, value);
        }
    }

    @GenerateUncached
    abstract static class HashingStorageToKeywords extends PNodeWithContext {
        public abstract PKeyword[] execute(HashingStorage storage);

        @Specialization
        static PKeyword[] doKeywordsStorage(KeywordsStorage storage) {
            return storage.getStore();
        }

        @Specialization
        static PKeyword[] doEmptyStorage(@SuppressWarnings("unused") EmptyStorage storage) {
            return PKeyword.EMPTY_KEYWORDS;
        }

        @Specialization(guards = {"len(lenNode, storage) == cachedLen", "cachedLen < 32", "!isKeywordsStorage(storage)",
                        "!isEmptyStorage(storage)"}, limit = "getVariableArgumentInlineCacheLimit()")
        static PKeyword[] doCached(HashingStorage storage,
                        @SuppressWarnings("unused") @Cached GetClassNode getClassNode,
                        @SuppressWarnings("unused") @Cached(parameters = "Iter") LookupCallableSlotInMRONode lookupIter,
                        @Cached AddKeywordNode addKeywordNode,
                        @Cached HashingStorageNodes.HashingStorageForEach forEachNode,
                        @SuppressWarnings("unused") @Cached HashingStorageLen lenNode,
                        @Cached("len(lenNode, storage)") int cachedLen) {
            PKeyword[] keywords = PKeyword.create(cachedLen);
            forEachNode.execute(null, storage, addKeywordNode, new CopyKeywordsState(keywords));
            return keywords;
        }

        @Specialization(guards = {"!isKeywordsStorage(storage)", "!isEmptyStorage(storage)"}, replaces = "doCached")
        static PKeyword[] doGeneric(HashingStorage storage,
                        @SuppressWarnings("unused") @Cached GetClassNode getClassNode,
                        @SuppressWarnings("unused") @Cached(parameters = "Iter") LookupCallableSlotInMRONode lookupIter,
                        @Cached AddKeywordNode addKeywordNode,
                        @Cached HashingStorageNodes.HashingStorageForEach forEachNode,
                        @Cached HashingStorageLen lenNode) {
            return doCached(storage, getClassNode, lookupIter, addKeywordNode, forEachNode, lenNode, len(lenNode, storage));
        }

        static boolean isKeywordsStorage(HashingStorage storage) {
            return storage instanceof KeywordsStorage;
        }

        static boolean isEmptyStorage(HashingStorage storage) {
            return storage instanceof EmptyStorage;
        }

        static int len(HashingStorageLen lenNode, HashingStorage storage) {
            return lenNode.execute(storage);
        }
    }
}
