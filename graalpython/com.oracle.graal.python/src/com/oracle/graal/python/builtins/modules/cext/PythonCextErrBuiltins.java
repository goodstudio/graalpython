/*
 * Copyright (c) 2022, 2024, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.python.builtins.modules.cext;

import static com.oracle.graal.python.builtins.PythonBuiltinClassType.SystemError;
import static com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiCallPath.Direct;
import static com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiCallPath.Ignored;
import static com.oracle.graal.python.builtins.modules.io.IONodes.T_FLUSH;
import static com.oracle.graal.python.builtins.modules.io.IONodes.T_WRITE;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.ConstCharPtrAsTruffleString;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.Int;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyObjectBorrowed;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyObjectTransfer;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyThreadState;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.Void;
import static com.oracle.graal.python.builtins.objects.exception.PBaseException.T_CODE;
import static com.oracle.graal.python.nodes.BuiltinNames.T_EXCEPTHOOK;
import static com.oracle.graal.python.nodes.BuiltinNames.T_LAST_TRACEBACK;
import static com.oracle.graal.python.nodes.BuiltinNames.T_LAST_TYPE;
import static com.oracle.graal.python.nodes.BuiltinNames.T_LAST_VALUE;
import static com.oracle.graal.python.nodes.ErrorMessages.EXCEPTION_NOT_BASEEXCEPTION;
import static com.oracle.graal.python.nodes.ErrorMessages.MUST_BE_MODULE_CLASS;
import static com.oracle.graal.python.nodes.SpecialAttributeNames.T___CAUSE__;
import static com.oracle.graal.python.nodes.SpecialAttributeNames.T___CONTEXT__;
import static com.oracle.graal.python.nodes.SpecialAttributeNames.T___DOC__;
import static com.oracle.graal.python.nodes.SpecialAttributeNames.T___MODULE__;
import static com.oracle.graal.python.nodes.SpecialAttributeNames.T___TRACEBACK__;
import static com.oracle.graal.python.util.PythonUtils.TS_ENCODING;

import com.oracle.graal.python.PythonLanguage;
import com.oracle.graal.python.builtins.PythonBuiltinClassType;
import com.oracle.graal.python.builtins.modules.BuiltinConstructors.TypeNode;
import com.oracle.graal.python.builtins.modules.BuiltinFunctions.IsInstanceNode;
import com.oracle.graal.python.builtins.modules.BuiltinFunctions.IsSubClassNode;
import com.oracle.graal.python.builtins.modules.PosixModuleBuiltins.ExitNode;
import com.oracle.graal.python.builtins.modules.SysModuleBuiltins;
import com.oracle.graal.python.builtins.modules.SysModuleBuiltins.ExcInfoNode;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiBinaryBuiltinNode;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiBuiltin;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiNullaryBuiltinNode;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiQuaternaryBuiltinNode;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiTernaryBuiltinNode;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiUnaryBuiltinNode;
import com.oracle.graal.python.builtins.modules.cext.PythonCextFileBuiltins.PyFile_WriteObject;
import com.oracle.graal.python.builtins.objects.PNone;
import com.oracle.graal.python.builtins.objects.cext.capi.CExtNodes.ClearCurrentExceptionNode;
import com.oracle.graal.python.builtins.objects.cext.capi.CExtNodes.TransformExceptionToNativeNode;
import com.oracle.graal.python.builtins.objects.cext.capi.PThreadState;
import com.oracle.graal.python.builtins.objects.cext.common.NativePointer;
import com.oracle.graal.python.builtins.objects.common.HashingStorageNodes.HashingStorageGetItem;
import com.oracle.graal.python.builtins.objects.dict.DictBuiltins.SetItemNode;
import com.oracle.graal.python.builtins.objects.dict.PDict;
import com.oracle.graal.python.builtins.objects.exception.ExceptionNodes;
import com.oracle.graal.python.builtins.objects.exception.PBaseException;
import com.oracle.graal.python.builtins.objects.exception.PrepareExceptionNode;
import com.oracle.graal.python.builtins.objects.function.PKeyword;
import com.oracle.graal.python.builtins.objects.ints.PInt;
import com.oracle.graal.python.builtins.objects.module.PythonModule;
import com.oracle.graal.python.builtins.objects.traceback.LazyTraceback;
import com.oracle.graal.python.builtins.objects.traceback.MaterializeLazyTracebackNode;
import com.oracle.graal.python.builtins.objects.traceback.PTraceback;
import com.oracle.graal.python.builtins.objects.tuple.PTuple;
import com.oracle.graal.python.builtins.objects.tuple.TupleBuiltins;
import com.oracle.graal.python.builtins.objects.tuple.TupleBuiltins.GetItemNode;
import com.oracle.graal.python.builtins.objects.type.TypeNodes.IsTypeNode;
import com.oracle.graal.python.lib.PyErrExceptionMatchesNode;
import com.oracle.graal.python.lib.PyObjectCallMethodObjArgs;
import com.oracle.graal.python.lib.PyObjectGetAttr;
import com.oracle.graal.python.lib.PyObjectLookupAttr;
import com.oracle.graal.python.lib.PyObjectSetAttr;
import com.oracle.graal.python.lib.PyObjectStrAsObjectNode;
import com.oracle.graal.python.nodes.PRaiseNode;
import com.oracle.graal.python.nodes.WriteUnraisableNode;
import com.oracle.graal.python.nodes.attributes.WriteAttributeToObjectNode;
import com.oracle.graal.python.nodes.call.CallNode;
import com.oracle.graal.python.nodes.object.BuiltinClassProfiles.IsBuiltinObjectProfile;
import com.oracle.graal.python.nodes.object.GetClassNode;
import com.oracle.graal.python.nodes.util.ExceptionStateNodes.GetCaughtExceptionNode;
import com.oracle.graal.python.runtime.PythonContext;
import com.oracle.graal.python.runtime.PythonContext.GetThreadStateNode;
import com.oracle.graal.python.runtime.PythonOptions;
import com.oracle.graal.python.runtime.exception.ExceptionUtils;
import com.oracle.graal.python.runtime.exception.PException;
import com.oracle.graal.python.runtime.exception.PythonErrorType;
import com.oracle.graal.python.runtime.object.PythonObjectFactory;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import com.oracle.truffle.api.strings.TruffleString;

public final class PythonCextErrBuiltins {
    private static Object noneToNativeNull(Node node, Object obj) {
        return obj instanceof PNone ? PythonContext.get(node).getNativeNull() : obj;
    }

    @CApiBuiltin(ret = Void, args = {PyObject, PyObject, PyObject}, call = Direct)
    abstract static class PyErr_Restore extends CApiTernaryBuiltinNode {

        @Specialization
        Object restore(Object typ, Object val, Object tb,
                        @Bind("this") Node inliningTarget,
                        @Cached GetThreadStateNode getThreadStateNode,
                        @Cached PrepareExceptionNode prepareExceptionNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Cached ClearCurrentExceptionNode clearCurrentExceptionNode) {
            if (typ == PNone.NO_VALUE && val == PNone.NO_VALUE) {
                clearCurrentExceptionNode.execute(inliningTarget, getThreadStateNode.execute(inliningTarget));
            } else {
                Object exception = prepareExceptionNode.execute(null, typ, val);
                PException e = PException.fromExceptionInfo(exception, PythonOptions.isPExceptionWithJavaStacktrace(getLanguage()));
                transformExceptionToNativeNode.execute(inliningTarget, e, tb instanceof PTraceback ptb ? new LazyTraceback(ptb) : null);
            }
            return PNone.NO_VALUE;
        }
    }

    @CApiBuiltin(ret = Void, args = {PyObject, PyObject, PyObject}, call = Direct)
    abstract static class _PyErr_ChainExceptions extends CApiTernaryBuiltinNode {
        @Specialization
        static Object run(Object typ, Object val, Object tb,
                        @Bind("this") Node inliningTarget,
                        @Cached GetThreadStateNode getThreadStateNode,
                        @Cached PrepareExceptionNode prepareExceptionNode,
                        @Cached ExceptionNodes.SetTracebackNode setTracebackNode,
                        @Cached ExceptionNodes.SetContextNode setContextNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode) {
            if (typ != PNone.NO_VALUE) {
                PythonContext.PythonThreadState threadState = getThreadStateNode.execute(inliningTarget, PythonContext.get(inliningTarget));
                Object exception;
                exception = prepareExceptionNode.execute(null, typ, val);
                if (threadState.getCurrentException() != null) {
                    if (tb != PNone.NO_VALUE) {
                        setTracebackNode.execute(inliningTarget, exception, tb);
                    }
                    Object currentException = threadState.getCurrentException().getEscapedException();
                    setContextNode.execute(inliningTarget, currentException, exception);
                } else {
                    PException e = PException.fromExceptionInfo(exception, PythonOptions.isPExceptionWithJavaStacktrace(PythonLanguage.get(inliningTarget)));
                    transformExceptionToNativeNode.execute(inliningTarget, e, tb instanceof PTraceback ptb ? new LazyTraceback(ptb) : null);
                }
            }
            return PNone.NO_VALUE;
        }
    }

    @CApiBuiltin(ret = PyObjectTransfer, call = Ignored)
    abstract static class PyTruffleErr_Fetch extends CApiNullaryBuiltinNode {
        @Specialization
        Object run(
                        @Bind("this") Node inliningTarget,
                        @Cached GetThreadStateNode getThreadStateNode,
                        @Cached GetClassNode getClassNode,
                        @Cached MaterializeLazyTracebackNode materializeTraceback,
                        @Cached PythonObjectFactory factory,
                        @Cached ClearCurrentExceptionNode clearCurrentExceptionNode) {
            PythonContext.PythonThreadState threadState = getThreadStateNode.execute(inliningTarget);
            PException currentException = threadState.getCurrentException();
            Object result;
            if (currentException == null) {
                result = getNativeNull();
            } else {
                Object exception = currentException.getEscapedException();
                Object traceback = null;
                if (threadState.getCurrentTraceback() != null) {
                    traceback = materializeTraceback.execute(inliningTarget, threadState.getCurrentTraceback());
                }
                if (traceback == null) {
                    traceback = getNativeNull();
                }
                result = factory.createTuple(new Object[]{getClassNode.execute(inliningTarget, exception), exception, traceback});
                clearCurrentExceptionNode.execute(inliningTarget, threadState);
            }
            return result;
        }
    }

    @CApiBuiltin(ret = PyObjectBorrowed, args = {PyThreadState}, call = Ignored)
    abstract static class _PyTruffleErr_Occurred extends CApiUnaryBuiltinNode {
        @Specialization
        Object run(PThreadState state,
                        @Bind("this") Node inliningTarget,
                        @Cached GetClassNode getClassNode) {
            PException currentException = state.getThreadState().getCurrentException();
            if (currentException != null) {
                // getClassNode acts as a branch profile
                return getClassNode.execute(inliningTarget, currentException.getUnreifiedException());
            }
            return getNativeNull();
        }
    }

    @CApiBuiltin(ret = Void, args = {PyObject, PyObject, PyObject}, call = Direct)
    abstract static class PyErr_SetExcInfo extends CApiTernaryBuiltinNode {
        @Specialization
        @SuppressWarnings("unused")
        Object doClear(Object typ, PNone val, Object tb) {
            PythonContext pythonContext = getContext();
            PythonLanguage lang = getLanguage();
            pythonContext.getThreadState(lang).setCaughtException(PException.NO_EXCEPTION);
            return PNone.NONE;
        }

        @Specialization
        Object doFull(@SuppressWarnings("unused") Object typ, PBaseException val, @SuppressWarnings("unused") Object tb) {
            PythonContext context = getContext();
            PythonLanguage language = getLanguage();
            PException e = PException.fromExceptionInfo(val, PythonOptions.isPExceptionWithJavaStacktrace(language));
            context.getThreadState(language).setCaughtException(e);
            return PNone.NONE;
        }

        @Fallback
        @SuppressWarnings("unused")
        Object doFallback(Object typ, Object val, Object tb) {
            // TODO we should still store the values to return them with 'PyErr_GetExcInfo' (or
            // 'sys.exc_info')
            return PNone.NONE;
        }
    }

    /**
     * Exceptions are usually printed using the traceback module or the hook function
     * {@code sys.excepthook}. This is the last resort if the hook function itself failed.
     */
    @CApiBuiltin(ret = Void, args = {PyObject, PyObject, PyObject}, call = Direct)
    abstract static class PyErr_Display extends CApiTernaryBuiltinNode {

        @Specialization
        @SuppressWarnings("unused")
        Object run(Object typ, PBaseException val, Object tb) {
            if (val.getException() != null) {
                ExceptionUtils.printPythonLikeStackTrace(val.getException());
            }
            return PNone.NO_VALUE;
        }
    }

    @CApiBuiltin(ret = Void, args = {PyObject, PyObject}, call = Direct)
    abstract static class _PyTruffleErr_CreateAndSetException extends CApiBinaryBuiltinNode {
        @Specialization(guards = "!isExceptionClass(inliningTarget, type, isTypeNode, isSubClassNode)")
        static Object create(Object type, @SuppressWarnings("unused") Object value,
                        @SuppressWarnings("unused") @Bind("this") Node inliningTarget,
                        @SuppressWarnings("unused") @Shared @Cached IsTypeNode isTypeNode,
                        @SuppressWarnings("unused") @Shared @Cached IsSubClassNode isSubClassNode,
                        @Cached PRaiseNode raiseNode) {
            throw raiseNode.raise(PythonBuiltinClassType.SystemError, EXCEPTION_NOT_BASEEXCEPTION, new Object[]{type});
        }

        @Specialization(guards = "isExceptionClass(inliningTarget, type, isTypeNode, isSubClassNode)")
        static Object create(Object type, Object value,
                        @Bind("this") Node inliningTarget,
                        @SuppressWarnings("unused") @Shared @Cached IsTypeNode isTypeNode,
                        @SuppressWarnings("unused") @Shared @Cached IsSubClassNode isSubClassNode,
                        @Cached PrepareExceptionNode prepareExceptionNode) {
            Object exception = prepareExceptionNode.execute(null, type, value);
            throw PRaiseNode.raiseExceptionObject(inliningTarget, exception);
        }

        protected static boolean isExceptionClass(Node inliningTarget, Object obj, IsTypeNode isTypeNode, IsSubClassNode isSubClassNode) {
            return isTypeNode.execute(inliningTarget, obj) && isSubClassNode.executeWith(null, obj, PythonBuiltinClassType.PBaseException);
        }
    }

    @CApiBuiltin(ret = PyObjectTransfer, args = {ConstCharPtrAsTruffleString, PyObject, PyObject}, call = Direct)
    abstract static class PyErr_NewException extends CApiTernaryBuiltinNode {

        @Specialization
        static Object newEx(TruffleString name, Object base, Object dict,
                        @Bind("this") Node inliningTarget,
                        @Cached HashingStorageGetItem getItem,
                        @Cached TruffleString.IndexOfCodePointNode indexOfCodepointNode,
                        @Cached TruffleString.CodePointLengthNode codePointLengthNode,
                        @Cached TruffleString.SubstringNode substringNode,
                        @Cached SetItemNode setItemNode,
                        @Cached TypeNode typeNode,
                        @Cached InlinedBranchProfile notDotProfile,
                        @Cached InlinedBranchProfile notModuleProfile,
                        @Cached InlinedConditionProfile baseProfile,
                        @Cached PythonObjectFactory.Lazy factory,
                        @Cached PRaiseNode.Lazy raiseNode) {
            if (base == PNone.NO_VALUE) {
                base = PythonErrorType.Exception;
            }
            if (dict == PNone.NO_VALUE) {
                dict = factory.get(inliningTarget).createDict();
            }
            int length = codePointLengthNode.execute(name, TS_ENCODING);
            int dotIdx = indexOfCodepointNode.execute(name, '.', 0, length, TS_ENCODING);
            if (dotIdx < 0) {
                notDotProfile.enter(inliningTarget);
                throw raiseNode.get(inliningTarget).raise(SystemError, MUST_BE_MODULE_CLASS, "PyErr_NewException", "name");
            }
            if (getItem.execute(null, inliningTarget, ((PDict) dict).getDictStorage(), base) == null) {
                notModuleProfile.enter(inliningTarget);
                setItemNode.execute(null, dict, T___MODULE__, substringNode.execute(name, 0, dotIdx, TS_ENCODING, false));
            }
            PTuple bases;
            if (baseProfile.profile(inliningTarget, base instanceof PTuple)) {
                bases = (PTuple) base;
            } else {
                bases = factory.get(inliningTarget).createTuple(new Object[]{base});
            }
            return typeNode.execute(null, PythonBuiltinClassType.PythonClass, substringNode.execute(name, dotIdx + 1, length - dotIdx - 1, TS_ENCODING, false), bases, dict,
                            PKeyword.EMPTY_KEYWORDS);
        }
    }

    @CApiBuiltin(ret = PyObjectTransfer, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, PyObject, PyObject}, call = Direct)
    abstract static class PyErr_NewExceptionWithDoc extends CApiQuaternaryBuiltinNode {

        @Specialization
        static Object raise(TruffleString name, Object doc, Object base, Object dict,
                        @Cached PyErr_NewException newExNode,
                        @Cached WriteAttributeToObjectNode writeAtrrNode,
                        @Cached PythonObjectFactory factory) {
            if (base == PNone.NO_VALUE) {
                base = PythonErrorType.Exception;
            }
            if (dict == PNone.NO_VALUE) {
                dict = factory.createDict();
            }
            Object ex = newExNode.execute(name, base, dict);
            if (doc != PNone.NO_VALUE) {
                writeAtrrNode.execute(ex, T___DOC__, doc);
            }
            return ex;
        }
    }

    @CApiBuiltin(ret = PyObjectTransfer, call = Ignored)
    abstract static class PyTruffleErr_GetExcInfo extends CApiNullaryBuiltinNode {
        @Specialization
        Object info(
                        @Bind("this") Node inliningTarget,
                        @Cached GetCaughtExceptionNode getCaughtExceptionNode,
                        @Cached GetClassNode getClassNode,
                        @Cached ExceptionNodes.GetTracebackNode getTracebackNode,
                        @Cached InlinedBranchProfile noExceptionProfile,
                        @Cached PythonObjectFactory factory) {
            PException currentException = getCaughtExceptionNode.executeFromNative();
            if (currentException == null) {
                noExceptionProfile.enter(inliningTarget);
                return getNativeNull();
            }
            assert currentException != PException.NO_EXCEPTION;
            Object exception = currentException.getEscapedException();
            Object traceback = noneToNativeNull(inliningTarget, getTracebackNode.execute(inliningTarget, exception));
            return factory.createTuple(new Object[]{getClassNode.execute(inliningTarget, exception), exception, traceback});
        }
    }

    @CApiBuiltin(ret = Int, args = {PyObject, PyObject}, call = Direct)
    abstract static class PyErr_GivenExceptionMatches extends CApiBinaryBuiltinNode {

        @Specialization
        static int matches(Object err, Object exc,
                        @Bind("this") Node inliningTarget,
                        @Cached PyErrExceptionMatchesNode matchesNode) {
            return PInt.intValue(matchesNode.execute(inliningTarget, err, exc));
        }
    }

    @CApiBuiltin(ret = Void, args = {ConstCharPtrAsTruffleString, PyObject}, call = Direct)
    abstract static class _PyErr_WriteUnraisableMsg extends CApiBinaryBuiltinNode {
        @Specialization
        static Object write(Object msg, Object obj,
                        @Bind("this") Node inliningTarget,
                        @Cached GetThreadStateNode getThreadStateNode,
                        @Cached WriteUnraisableNode writeUnraisableNode,
                        @Cached ClearCurrentExceptionNode clearCurrentExceptionNode) {
            PythonContext.PythonThreadState threadState = getThreadStateNode.execute(inliningTarget, PythonContext.get(inliningTarget));
            if (threadState.getCurrentException() == null) {
                // This means an invalid call, but this function is not supposed to raise exceptions
                return PNone.NONE;
            }
            threadState.syncTracebackToException();
            Object exc = threadState.getCurrentException().getEscapedException();
            TruffleString m = null;
            if (msg instanceof TruffleString) {
                m = (TruffleString) msg;
            }
            writeUnraisableNode.execute(exc, m, (obj == PNone.NO_VALUE) ? PNone.NONE : obj);
            clearCurrentExceptionNode.execute(inliningTarget, threadState);
            return PNone.NONE;
        }
    }

    @CApiBuiltin(ret = Void, args = {Int}, call = Direct)
    abstract static class PyErr_PrintEx extends CApiUnaryBuiltinNode {
        @TruffleBoundary
        @Specialization
        static Object raise(int set_sys_last_vars,
                        @Cached _PyTruffleErr_Occurred errOccuredNode,
                        @Cached TupleBuiltins.GetItemNode getItemNode,
                        @Cached IsInstanceNode isInstanceNode,
                        @Cached ExcInfoNode excInfoNode,
                        @Cached PyErr_Restore restoreNode,
                        @Cached PyFile_WriteObject writeFileNode,
                        @Cached ExitNode exitNode,
                        @Cached PyTruffleErr_Fetch fetchNode,
                        @Cached PyErr_Display errDisplayNode) {
            PythonContext context = PythonContext.get(null);
            NativePointer nativeNull = context.getNativeNull();

            Object err = errOccuredNode.execute(context.getThreadState(PythonLanguage.get(null)));
            PythonModule sys = context.getSysModule();
            if (err != nativeNull && IsBuiltinObjectProfile.profileObjectUncached(err, PythonBuiltinClassType.SystemExit)) {
                handleSystemExit(excInfoNode, getItemNode, isInstanceNode, restoreNode, (SysModuleBuiltins) sys.getBuiltins(), writeFileNode, exitNode);
            }
            Object fetched = fetchNode.execute(null);
            Object type = null;
            Object val = null;
            Object tb = null;

            if (fetched != nativeNull) {
                PTuple fetchedTuple = (PTuple) fetched;
                type = getItemNode.execute(null, fetchedTuple, 0);
                val = getItemNode.execute(null, fetchedTuple, 1);
                tb = getItemNode.execute(null, fetchedTuple, 2);
            }
            if (type == null || type == PNone.NONE) {
                return PNone.NONE;
            }
            if (tb == nativeNull) {
                tb = PNone.NONE;
            }
            if (PyObjectLookupAttr.executeUncached(val, T___TRACEBACK__) == PNone.NONE) {
                WriteAttributeToObjectNode.getUncached().execute(val, T___TRACEBACK__, tb);
            }

            if (set_sys_last_vars != 0) {
                writeLastVars(sys, type, val, tb, restoreNode);
            }
            Object exceptHook = PyObjectLookupAttr.executeUncached(sys, T_EXCEPTHOOK);
            if (exceptHook != PNone.NO_VALUE) {
                handleExceptHook(exceptHook, type, val, tb, excInfoNode, getItemNode, sys, errDisplayNode);
            }
            return PNone.NONE;
        }

        private static void writeLastVars(PythonModule sys, Object type, Object val, Object tb, PyErr_Restore restoreNode) {
            CompilerAsserts.neverPartOfCompilation();
            try {
                WriteAttributeToObjectNode.getUncached().execute(sys, T_LAST_TYPE, type);
                WriteAttributeToObjectNode.getUncached().execute(sys, T_LAST_VALUE, val);
                WriteAttributeToObjectNode.getUncached().execute(sys, T_LAST_TRACEBACK, tb);
            } catch (PException e) {
                restoreNode.execute(PNone.NONE, PNone.NONE, PNone.NONE);
            }
        }

        private static void handleExceptHook(Object exceptHook, Object type, Object val, Object tb, ExcInfoNode excInfoNode,
                        GetItemNode getItemNode, PythonModule sys, PyErr_Display errDisplayNode) {
            CompilerAsserts.neverPartOfCompilation();
            try {
                CallNode.getUncached().execute(exceptHook, type, val, tb);
            } catch (PException e) {
                PTuple sysInfo = excInfoNode.execute(null);
                Object type1 = getItemNode.execute(null, sysInfo, 0);
                Object val1 = getItemNode.execute(null, sysInfo, 1);
                Object tb1 = getItemNode.execute(null, sysInfo, 2);
                // not quite the same as 'PySys_WriteStderr' but close
                Object stdErr = ((SysModuleBuiltins) sys.getBuiltins()).getStdErr();
                Object writeMethod = PyObjectGetAttr.executeUncached(stdErr, T_WRITE);
                CallNode.getUncached().execute(null, writeMethod, PyObjectStrAsObjectNode.getUncached().execute(null, "Error in sys.excepthook:\n"));
                errDisplayNode.execute(type1, val1, tb1);
                CallNode.getUncached().execute(null, writeMethod, PyObjectStrAsObjectNode.getUncached().execute(null, "\nOriginal exception was:\n"));
                errDisplayNode.execute(type, val, tb);
                PyObjectCallMethodObjArgs.executeUncached(stdErr, T_FLUSH);
            }
        }

        private static void handleSystemExit(ExcInfoNode excInfoNode, TupleBuiltins.GetItemNode getItemNode, IsInstanceNode isInstanceNode,
                        PyErr_Restore restoreNode, SysModuleBuiltins sys, PyFile_WriteObject writeFileNode, ExitNode exitNode) {
            CompilerAsserts.neverPartOfCompilation();
            PTuple sysInfo = excInfoNode.execute(null);
            int rc = 0;
            Object returnObject = null;
            Object val = getItemNode.execute(null, sysInfo, 1);
            Object codeAttr = PyObjectLookupAttr.executeUncached(val, T_CODE);
            if (val != PNone.NONE && !(codeAttr instanceof PNone)) {
                returnObject = codeAttr;
            }
            if (!(codeAttr instanceof PNone) && isInstanceNode.executeWith(null, codeAttr, PythonBuiltinClassType.PInt)) {
                rc = (int) codeAttr;
            } else {
                restoreNode.execute(PNone.NONE, PNone.NONE, PNone.NONE);
                Object stdErr = sys.getStdErr();
                if (stdErr != null && stdErr != PNone.NONE) {
                    writeFileNode.execute(returnObject, stdErr, 1);
                } else {
                    Object stdOut = sys.getStdOut();
                    Object writeMethod = PyObjectGetAttr.executeUncached(stdOut, T_WRITE);
                    CallNode.getUncached().execute(null, writeMethod, PyObjectStrAsObjectNode.getUncached().execute(null, returnObject));
                    PyObjectCallMethodObjArgs.executeUncached(stdOut, T_FLUSH);
                }
            }
            exitNode.execute(null, rc);
        }
    }

    @CApiBuiltin(ret = Void, args = {PyObject, PyObject}, call = Direct)
    abstract static class PyException_SetCause extends CApiBinaryBuiltinNode {
        @Specialization
        Object setCause(Object exc, Object cause,
                        @Bind("this") Node inliningTarget,
                        @Cached PyObjectSetAttr setAttrNode) {
            setAttrNode.execute(inliningTarget, exc, T___CAUSE__, cause);
            return PNone.NONE;
        }
    }

    @CApiBuiltin(ret = PyObjectTransfer, args = {PyObject}, call = Direct)
    abstract static class PyException_GetCause extends CApiUnaryBuiltinNode {
        @Specialization
        Object getCause(Object exc,
                        @Bind("this") Node inliningTarget,
                        @Cached PyObjectGetAttr getAttrNode) {
            return noneToNativeNull(inliningTarget, getAttrNode.execute(inliningTarget, exc, T___CAUSE__));
        }
    }

    @CApiBuiltin(ret = PyObjectTransfer, args = {PyObject}, call = Direct)
    abstract static class PyException_GetContext extends CApiUnaryBuiltinNode {
        @Specialization
        Object setCause(Object exc,
                        @Bind("this") Node inliningTarget,
                        @Cached PyObjectGetAttr getAttrNode) {
            return noneToNativeNull(inliningTarget, getAttrNode.execute(inliningTarget, exc, T___CONTEXT__));
        }
    }

    @CApiBuiltin(ret = Void, args = {PyObject, PyObject}, call = Direct)
    abstract static class PyException_SetContext extends CApiBinaryBuiltinNode {
        @Specialization
        Object setContext(Object exc, Object context,
                        @Bind("this") Node inliningTarget,
                        @Cached PyObjectSetAttr setAttrNode) {
            setAttrNode.execute(inliningTarget, exc, T___CONTEXT__, context);
            return PNone.NONE;
        }
    }

    @CApiBuiltin(ret = PyObjectTransfer, args = {PyObject}, call = Direct)
    abstract static class PyException_GetTraceback extends CApiUnaryBuiltinNode {

        @Specialization
        Object getTraceback(Object exc,
                        @Bind("this") Node inliningTarget,
                        @Cached PyObjectGetAttr getAttrNode) {
            return noneToNativeNull(inliningTarget, getAttrNode.execute(inliningTarget, exc, T___TRACEBACK__));
        }
    }

    @CApiBuiltin(ret = Int, args = {PyObject, PyObject}, call = Direct)
    abstract static class PyException_SetTraceback extends CApiBinaryBuiltinNode {

        @Specialization
        Object setTraceback(Object exc, Object traceback,
                        @Bind("this") Node inliningTarget,
                        @Cached PyObjectSetAttr setAttrNode) {
            setAttrNode.execute(inliningTarget, exc, T___TRACEBACK__, traceback);
            return 0;
        }
    }
}
