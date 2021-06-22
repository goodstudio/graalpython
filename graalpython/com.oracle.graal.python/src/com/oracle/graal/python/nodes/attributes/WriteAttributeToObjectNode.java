/*
 * Copyright (c) 2017, 2021, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.python.nodes.attributes;

import static com.oracle.graal.python.runtime.exception.PythonErrorType.TypeError;

import com.oracle.graal.python.PythonLanguage;
import com.oracle.graal.python.builtins.PythonBuiltinClassType;
import com.oracle.graal.python.builtins.objects.cext.PythonAbstractNativeObject;
import com.oracle.graal.python.builtins.objects.cext.capi.CExtNodes.GetTypeMemberNode;
import com.oracle.graal.python.builtins.objects.cext.capi.NativeMember;
import com.oracle.graal.python.builtins.objects.common.HashingStorage;
import com.oracle.graal.python.builtins.objects.common.HashingStorageLibrary;
import com.oracle.graal.python.builtins.objects.dict.PDict;
import com.oracle.graal.python.builtins.objects.object.PythonObject;
import com.oracle.graal.python.builtins.objects.object.PythonObjectLibrary;
import com.oracle.graal.python.builtins.objects.type.PythonBuiltinClass;
import com.oracle.graal.python.builtins.objects.type.PythonClass;
import com.oracle.graal.python.builtins.objects.type.PythonManagedClass;
import com.oracle.graal.python.builtins.objects.type.SpecialMethodSlot;
import com.oracle.graal.python.builtins.objects.type.TypeNodes.IsTypeNode;
import com.oracle.graal.python.nodes.ErrorMessages;
import com.oracle.graal.python.nodes.PRaiseNode;
import com.oracle.graal.python.nodes.attributes.WriteAttributeToObjectNodeGen.WriteAttributeToObjectNotTypeNodeGen;
import com.oracle.graal.python.nodes.attributes.WriteAttributeToObjectNodeGen.WriteAttributeToObjectTpDictNodeGen;
import com.oracle.graal.python.nodes.util.CannotCastException;
import com.oracle.graal.python.nodes.util.CastToJavaStringNode;
import com.oracle.graal.python.runtime.PythonContext;
import com.oracle.graal.python.runtime.PythonOptions;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.HiddenKey;
import com.oracle.truffle.api.profiles.BranchProfile;

@ImportStatic(PythonOptions.class)
public abstract class WriteAttributeToObjectNode extends ObjectAttributeNode {

    public abstract boolean execute(Object primary, Object key, Object value);

    public abstract boolean execute(Object primary, HiddenKey key, Object value);

    public static WriteAttributeToObjectNode create() {
        return WriteAttributeToObjectNotTypeNodeGen.create();
    }

    public static WriteAttributeToObjectNode create(boolean forceType) {
        if (forceType) {
            return WriteAttributeToObjectTpDictNodeGen.create();
        }
        return WriteAttributeToObjectNotTypeNodeGen.create();
    }

    public static WriteAttributeToObjectNode createForceType() {
        return WriteAttributeToObjectTpDictNodeGen.create();
    }

    public static WriteAttributeToObjectNode getUncached() {
        return WriteAttributeToObjectNotTypeNodeGen.getUncached();
    }

    public static WriteAttributeToObjectNode getUncached(boolean forceType) {
        if (forceType) {
            return WriteAttributeToObjectTpDictNodeGen.getUncached();
        }
        return WriteAttributeToObjectNotTypeNodeGen.getUncached();
    }

    protected static boolean isAttrWritable(PythonObject self, Object key) {
        if (isHiddenKey(key)) {
            return true;
        }
        return (self.getShape().getFlags() & PythonObject.HAS_SLOTS_BUT_NO_DICT_FLAG) == 0;
    }

    private static String castKey(CastToJavaStringNode castNode, Object value) {
        try {
            return castNode.execute(value);
        } catch (CannotCastException ex) {
            throw CompilerDirectives.shouldNotReachHere(ex);
        }
    }

    protected static boolean writeToDynamicStorageNoTypeGuard(Object obj, Object key, BranchProfile isNotHidden, PythonObjectLibrary lib) {
        if (isHiddenKey(key)) {
            return true;
        }
        isNotHidden.enter();
        return !lib.hasDict(obj) && !PythonManagedClass.isInstance(obj);
    }

    // Specialization for cases that have no special handling and can just delegate to
    // WriteAttributeToDynamicObjectNode. Note that the fast-path for String keys and the inline
    // cache in WriteAttributeToDynamicObjectNode perform better in some configurations than if we
    // cast the key here and used DynamicObjectLibrary directly
    @Specialization(guards = {"isAttrWritable(object, key)", "writeToDynamicStorageNoTypeGuard(object, key, isNotHidden, lib)"}, limit = "1")
    static boolean writeToDynamicStorageNoType(PythonObject object, Object key, Object value,
                    @SuppressWarnings("unused") @Cached BranchProfile isNotHidden,
                    @CachedLibrary("object") @SuppressWarnings("unused") PythonObjectLibrary lib,
                    @Cached WriteAttributeToDynamicObjectNode writeNode) {
        // Objects w/o dict that are not classes do not have any special handling
        writeNode.execute(object, key, value);
        return true;
    }

    // Specializations for no dict & PythonManagedClass -> requires calling onAttributeUpdate
    @Specialization(guards = {"isAttrWritable(klass, key)", "!isHiddenKey(key)", "!lib.hasDict(klass)"}, limit = "1")
    static boolean writeToDynamicStorageBuiltinType(PythonBuiltinClass klass, Object key, Object value,
                    @CachedContext(PythonLanguage.class) PythonContext context,
                    @CachedLibrary("klass") @SuppressWarnings("unused") PythonObjectLibrary lib,
                    @Cached CastToJavaStringNode castToStrNode,
                    @Cached BranchProfile callAttrUpdate,
                    @CachedLibrary(limit = "getAttributeAccessInlineCacheMaxDepth()") DynamicObjectLibrary dylib) {
        if (context.isInitialized()) {
            throw context.getCore().raise(TypeError, ErrorMessages.CANT_SET_ATTRIBUTES_OF_TYPE_S, klass);
        } else {
            return writeToDynamicStorageManagedClass(klass, key, value, castToStrNode, callAttrUpdate, dylib);
        }
    }

    @Specialization(guards = {"isAttrWritable(klass, key)", "!isHiddenKey(key)", "!lib.hasDict(klass)"}, limit = "1")
    static boolean writeToDynamicStoragePythonClass(PythonClass klass, Object key, Object value,
                    @CachedLibrary("klass") @SuppressWarnings("unused") PythonObjectLibrary lib,
                    @Cached CastToJavaStringNode castToStrNode,
                    @Cached BranchProfile callAttrUpdate,
                    @CachedLibrary(limit = "getAttributeAccessInlineCacheMaxDepth()") DynamicObjectLibrary dylib) {
        return writeToDynamicStorageManagedClass(klass, key, value, castToStrNode, callAttrUpdate, dylib);
    }

    private static boolean writeToDynamicStorageManagedClass(PythonManagedClass klass, Object key, Object value, CastToJavaStringNode castToStrNode, BranchProfile callAttrUpdate,
                    DynamicObjectLibrary dylib) {
        CompilerAsserts.partialEvaluationConstant(klass.getClass());
        String strKey = castKey(castToStrNode, key);
        try {
            dylib.put(klass, strKey, value);
            return true;
        } finally {
            if (!klass.canSkipOnAttributeUpdate(strKey, value)) {
                callAttrUpdate.enter();
                klass.onAttributeUpdate(strKey, value);
            }
        }
    }

    // write to the dict: the basic specialization for non-classes
    @Specialization(guards = {"!isHiddenKey(key)", "lib.hasDict(object)", "!isManagedClass(object)"}, limit = "1")
    static boolean writeToDictNoType(PythonObject object, Object key, Object value,
                    @CachedLibrary("object") PythonObjectLibrary lib,
                    @Cached BranchProfile updateStorage,
                    @CachedLibrary(limit = "1") HashingStorageLibrary hlib) {
        return writeToDict(lib.getDict(object), key, value, updateStorage, hlib);
    }

    // write to the dict & PythonManagedClass -> requires calling onAttributeUpdate
    @Specialization(guards = {"!isHiddenKey(key)", "lib.hasDict(klass)"}, limit = "1")
    static boolean writeToDictBuiltinType(PythonBuiltinClass klass, Object key, Object value,
                    @CachedContext(PythonLanguage.class) PythonContext context,
                    @Cached CastToJavaStringNode castToStrNode,
                    @Cached BranchProfile callAttrUpdate,
                    @CachedLibrary("klass") PythonObjectLibrary lib,
                    @Cached BranchProfile updateStorage,
                    @CachedLibrary(limit = "1") HashingStorageLibrary hlib) {
        if (context.isInitialized()) {
            throw context.getCore().raise(TypeError, ErrorMessages.CANT_SET_ATTRIBUTES_OF_TYPE_S, klass);
        } else {
            return writeToDictManagedClass(klass, key, value, castToStrNode, callAttrUpdate, lib, updateStorage, hlib);
        }
    }

    @Specialization(guards = {"!isHiddenKey(key)", "lib.hasDict(klass)"}, limit = "1")
    static boolean writeToDictClass(PythonClass klass, Object key, Object value,
                    @Cached CastToJavaStringNode castToStrNode,
                    @Cached BranchProfile callAttrUpdate,
                    @CachedLibrary("klass") PythonObjectLibrary lib,
                    @Cached BranchProfile updateStorage,
                    @CachedLibrary(limit = "1") HashingStorageLibrary hlib) {
        return writeToDictManagedClass(klass, key, value, castToStrNode, callAttrUpdate, lib, updateStorage, hlib);
    }

    private static boolean writeToDictManagedClass(PythonManagedClass klass, Object key, Object value, CastToJavaStringNode castToStrNode, BranchProfile callAttrUpdate, PythonObjectLibrary lib,
                    BranchProfile updateStorage, HashingStorageLibrary hlib) {
        CompilerAsserts.partialEvaluationConstant(klass.getClass());
        String strKey = castKey(castToStrNode, key);
        try {
            return writeToDict(lib.getDict(klass), strKey, value, updateStorage, hlib);
        } finally {
            if (!klass.canSkipOnAttributeUpdate(strKey, value)) {
                callAttrUpdate.enter();
                klass.onAttributeUpdate(strKey, value);
            }
        }
    }

    static boolean writeToDict(PDict dict, Object key, Object value,
                    BranchProfile updateStorage,
                    HashingStorageLibrary hlib) {
        assert dict != null;
        HashingStorage dictStorage = dict.getDictStorage();
        HashingStorage hashingStorage = hlib.setItem(dictStorage, key, value);
        if (dictStorage != hashingStorage) {
            updateStorage.enter();
            dict.setDictStorage(hashingStorage);
        }
        return true;
    }

    @Specialization(guards = "isErrorCase(lib, object, key)")
    static boolean doError(Object object, Object key, @SuppressWarnings("unused") Object value,
                    @CachedLibrary(limit = "1") @SuppressWarnings("unused") PythonObjectLibrary lib,
                    @Cached PRaiseNode raiseNode) {
        throw raiseNode.raise(PythonBuiltinClassType.AttributeError, ErrorMessages.OBJ_P_HAS_NO_ATTR_S, object, key);
    }

    @Specialization
    static boolean doPBCT(PythonBuiltinClassType object, Object key, Object value,
                    @CachedContext(PythonLanguage.class) ContextReference<PythonContext> contextRef,
                    @Cached WriteAttributeToObjectNode recursive) {
        return recursive.execute(contextRef.get().getCore().lookupType(object), key, value);
    }

    protected static boolean isErrorCase(PythonObjectLibrary lib, Object object, Object key) {
        if (object instanceof PythonObject) {
            PythonObject self = (PythonObject) object;
            if (isAttrWritable(self, key) && (isHiddenKey(key) || !lib.hasDict(self))) {
                return false;
            }
            if (!isHiddenKey(key) && lib.hasDict(self)) {
                return false;
            }
        }
        if (object instanceof PythonAbstractNativeObject && !isHiddenKey(key)) {
            return false;
        }
        if (object instanceof PythonBuiltinClassType) {
            return false;
        }
        return true;
    }

    @GenerateUncached
    protected abstract static class WriteAttributeToObjectNotTypeNode extends WriteAttributeToObjectNode {
        @Specialization(guards = {"!isHiddenKey(key)"}, limit = "1")
        static boolean writeNativeObject(PythonAbstractNativeObject object, Object key, Object value,
                        @CachedLibrary("object") PythonObjectLibrary lib,
                        @CachedLibrary(limit = "1") HashingStorageLibrary hlib,
                        @Cached BranchProfile updateStorage,
                        @Cached PRaiseNode raiseNode) {
            /*
             * The dict of native objects that stores the object attributes is located at 'objectPtr
             * + Py_TYPE(objectPtr)->tp_dictoffset'. 'PythonObjectLibrary.getDict' will exactly load
             * the dict from there.
             */
            PDict dict = lib.getDict(object);
            if (dict != null) {
                return writeToDict(dict, key, value, updateStorage, hlib);
            }
            throw raiseNode.raise(PythonBuiltinClassType.AttributeError, ErrorMessages.OBJ_P_HAS_NO_ATTR_S, object, key);
        }
    }

    @GenerateUncached
    @ImportStatic(SpecialMethodSlot.class)
    protected abstract static class WriteAttributeToObjectTpDictNode extends WriteAttributeToObjectNode {

        /*
         * Simplest case: the key object is a String (so it cannot be a hidden key) and it's not a
         * special method slot.
         */
        @Specialization(guards = "!canBeSpecial(keyObj)")
        static boolean writeNativeClassSimple(PythonAbstractNativeObject object, String keyObj, Object value,
                        @Shared("getNativeDict") @Cached GetTypeMemberNode getNativeDict,
                        @Shared("hlib") @CachedLibrary(limit = "1") HashingStorageLibrary hlib,
                        @Shared("updateStorage") @Cached BranchProfile updateStorage,
                        @Shared("raiseNode") @Cached PRaiseNode raiseNode) {
            /*
             * For native types, the type attributes are stored in a dict that is located in
             * 'typePtr->tp_dict'. So, this is different to a native object (that is not a type) and
             * we need to load the dict differently. We must not use 'PythonObjectLibrary.getDict'
             * here but read member 'tp_dict'.
             */
            Object dict = getNativeDict.execute(object, NativeMember.TP_DICT);
            if (dict instanceof PDict) {
                return writeToDict((PDict) dict, keyObj, value, updateStorage, hlib);
            }
            throw raiseNode.raise(PythonBuiltinClassType.AttributeError, ErrorMessages.OBJ_P_HAS_NO_ATTR_S, object, keyObj);
        }

        @Specialization(guards = "!isHiddenKey(keyObj)", replaces = "writeNativeClassSimple")
        static boolean writeNativeClassGeneric(PythonAbstractNativeObject object, Object keyObj, Object value,
                        @Shared("getNativeDict") @Cached GetTypeMemberNode getNativeDict,
                        @Shared("hlib") @CachedLibrary(limit = "1") HashingStorageLibrary hlib,
                        @Shared("updateStorage") @Cached BranchProfile updateStorage,
                        @Cached BranchProfile canBeSpecialSlot,
                        @Cached CastToJavaStringNode castKeyNode,
                        @Cached IsTypeNode isTypeNode,
                        @Shared("raiseNode") @Cached PRaiseNode raiseNode) {
            try {
                /*
                 * For native types, the type attributes are stored in a dict that is located in
                 * 'typePtr->tp_dict'. So, this is different to a native object (that is not a type)
                 * and we need to load the dict differently. We must not use
                 * 'PythonObjectLibrary.getDict' here but read member 'tp_dict'.
                 */
                Object dict = getNativeDict.execute(object, NativeMember.TP_DICT);
                if (dict instanceof PDict) {
                    return writeToDict((PDict) dict, keyObj, value, updateStorage, hlib);
                }
                throw raiseNode.raise(PythonBuiltinClassType.AttributeError, ErrorMessages.OBJ_P_HAS_NO_ATTR_S, object, keyObj);
            } finally {
                try {
                    String key = castKeyNode.execute(keyObj);
                    if (SpecialMethodSlot.canBeSpecial(key)) {
                        canBeSpecialSlot.enter();
                        SpecialMethodSlot slot = SpecialMethodSlot.findSpecialSlot(key);
                        if (slot != null && isTypeNode.execute(object)) {
                            SpecialMethodSlot.fixupSpecialMethodSlot(object, slot, value);
                        }
                    }
                } catch (CannotCastException e) {
                    // fall through; it cannot be a special method slot
                }
            }
        }
    }
}
