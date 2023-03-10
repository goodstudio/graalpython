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
package com.oracle.graal.python.builtins.objects.cext.capi;

import static com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.checkThrowableBeforeNative;

import com.oracle.graal.python.builtins.objects.PNone;
import com.oracle.graal.python.builtins.objects.PNotImplemented;
import com.oracle.graal.python.builtins.objects.cext.capi.CExtNodes.TransformExceptionToNativeNode;
import com.oracle.graal.python.builtins.objects.cext.capi.transitions.CApiTiming;
import com.oracle.graal.python.builtins.objects.cext.capi.transitions.CApiTransitions.NativeToPythonNode;
import com.oracle.graal.python.builtins.objects.cext.capi.transitions.CApiTransitions.PythonToNativeTransferNode;
import com.oracle.graal.python.builtins.objects.function.PKeyword;
import com.oracle.graal.python.lib.PyNumberAsSizeNode;
import com.oracle.graal.python.lib.PyNumberIndexNode;
import com.oracle.graal.python.lib.PyObjectSizeNode;
import com.oracle.graal.python.nodes.PRaiseNode;
import com.oracle.graal.python.nodes.argument.keywords.ExpandKeywordStarargsNode;
import com.oracle.graal.python.nodes.argument.positional.ExecutePositionalStarargsNode;
import com.oracle.graal.python.nodes.call.special.CallBinaryMethodNode;
import com.oracle.graal.python.nodes.call.special.CallTernaryMethodNode;
import com.oracle.graal.python.nodes.call.special.CallUnaryMethodNode;
import com.oracle.graal.python.nodes.call.special.CallVarargsMethodNode;
import com.oracle.graal.python.nodes.util.CastToJavaIntLossyNode;
import com.oracle.graal.python.runtime.GilNode;
import com.oracle.graal.python.runtime.PythonContext;
import com.oracle.graal.python.runtime.exception.PException;
import com.oracle.graal.python.util.PythonUtils;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.llvm.spi.NativeTypeLibrary;
import com.oracle.truffle.nfi.api.SignatureLibrary;

@ExportLibrary(InteropLibrary.class)
@ExportLibrary(value = NativeTypeLibrary.class, useForAOT = false)
public abstract class PyProcsWrapper extends PythonNativeWrapper {

    protected final CApiTiming timing;

    public PyProcsWrapper(Object delegate) {
        super(delegate);
        this.timing = CApiTiming.create(true, delegate);
    }

    @ExportMessage
    protected boolean isExecutable() {
        return true;
    }

    @ExportMessage
    @SuppressWarnings({"unused", "static-method"})
    protected Object execute(Object[] arguments) throws UnsupportedTypeException, ArityException, UnsupportedMessageException {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new IllegalStateException("should not reach");
    }

    @ExportMessage
    @SuppressWarnings("static-method")
    protected boolean hasNativeType() {
        // TODO implement native type
        return false;
    }

    @ExportMessage
    @SuppressWarnings("static-method")
    public Object getNativeType() {
        // TODO implement native type
        return null;
    }

    @ExportMessage
    protected boolean isPointer() {
        return isNative();
    }

    @ExportMessage
    protected long asPointer() {
        return getNativePointer();
    }

    protected abstract String getSignature();

    @ExportMessage
    @TruffleBoundary
    protected void toNative() {
        if (!isPointer()) {
            Object signature = PythonContext.get(null).getEnv().parseInternal(Source.newBuilder("nfi", "with panama " + getSignature(), "exec").build()).call();
            Object result = SignatureLibrary.getUncached().createClosure(signature, this);
            PythonContext.get(null).getCApiContext().retainClosure(result);
            setNativePointer(coerceToLong(result, InteropLibrary.getUncached()));
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class GetAttrWrapper extends PyProcsWrapper {

        public GetAttrWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                        @Cached PythonToNativeTransferNode PythonToNativeTransferNode,
                        @Cached CallBinaryMethodNode executeNode,
                        @Cached NativeToPythonNode toJavaNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Exclusive @Cached GilNode gil) throws ArityException {
            boolean mustRelease = gil.acquire();
            CApiTiming.enter();
            try {
                if (arguments.length != 2) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw ArityException.create(2, 2, arguments.length);
                }
                try {
                    return PythonToNativeTransferNode.execute(executeNode.executeObject(null, getDelegate(), toJavaNode.execute(arguments[0]), toJavaNode.execute(arguments[1])));
                } catch (Throwable t) {
                    throw checkThrowableBeforeNative(t, "GetAttrWrapper", getDelegate());
                }
            } catch (PException e) {
                transformExceptionToNativeNode.execute(null, e);
                return PythonContext.get(gil).getNativeNull().getPtr();
            } finally {
                CApiTiming.exit(timing);
                gil.release(mustRelease);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,POINTER):POINTER";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class BinaryFuncWrapper extends PyProcsWrapper {

        public BinaryFuncWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                        @Cached PythonToNativeTransferNode PythonToNativeTransferNode,
                        @Cached CallBinaryMethodNode executeNode,
                        @Cached NativeToPythonNode toJavaNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Exclusive @Cached GilNode gil) throws ArityException {
            boolean mustRelease = gil.acquire();
            CApiTiming.enter();
            try {
                if (arguments.length != 2) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw ArityException.create(2, 2, arguments.length);
                }
                try {
                    return PythonToNativeTransferNode.execute(executeNode.executeObject(null, getDelegate(), toJavaNode.execute(arguments[0]), toJavaNode.execute(arguments[1])));
                } catch (Throwable t) {
                    throw checkThrowableBeforeNative(t, "BinaryFuncWrapper", getDelegate());
                }
            } catch (PException e) {
                transformExceptionToNativeNode.execute(null, e);
                return PythonContext.get(gil).getNativeNull().getPtr();
            } finally {
                CApiTiming.exit(timing);
                gil.release(mustRelease);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,POINTER):POINTER";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class UnaryFuncWrapper extends PyProcsWrapper {

        public UnaryFuncWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                        @Cached PythonToNativeTransferNode PythonToNativeTransferNode,
                        @Cached CallUnaryMethodNode executeNode,
                        @Cached NativeToPythonNode toJavaNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Exclusive @Cached GilNode gil) throws ArityException {
            boolean mustRelease = gil.acquire();
            CApiTiming.enter();
            try {
                /*
                 * Accept a second argumenthere, since these functions are sometimes called using
                 * METH_O with a "NULL" value.
                 */
                if (arguments.length > 2) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw ArityException.create(1, 2, arguments.length);
                }
                try {
                    return PythonToNativeTransferNode.execute(executeNode.executeObject(null, getDelegate(), toJavaNode.execute(arguments[0])));
                } catch (Throwable t) {
                    throw checkThrowableBeforeNative(t, "UnaryFuncWrapper", getDelegate());
                }
            } catch (PException e) {
                transformExceptionToNativeNode.execute(null, e);
                return PythonContext.get(gil).getNativeNull().getPtr();
            } finally {
                CApiTiming.exit(timing);
                gil.release(mustRelease);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER):POINTER";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class InquiryWrapper extends PyProcsWrapper {

        public InquiryWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                        @Cached CallUnaryMethodNode executeNode,
                        @Cached NativeToPythonNode toJavaNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Exclusive @Cached GilNode gil) throws ArityException {
            boolean mustRelease = gil.acquire();
            CApiTiming.enter();
            try {
                if (arguments.length != 1) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw ArityException.create(1, 1, arguments.length);
                }
                try {
                    return executeNode.executeObject(null, getDelegate(), toJavaNode.execute(arguments[0]));
                } catch (Throwable t) {
                    throw checkThrowableBeforeNative(t, "InquiryWrapper", getDelegate());
                }
            } catch (PException e) {
                transformExceptionToNativeNode.execute(null, e);
                return -1;
            } finally {
                CApiTiming.exit(timing);
                gil.release(mustRelease);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER):SINT32";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class SetAttrWrapper extends PyProcsWrapper {

        public SetAttrWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage
        protected int execute(Object[] arguments,
                        @Cached CallTernaryMethodNode callTernaryMethodNode,
                        @Cached NativeToPythonNode toJavaNode,
                        @Cached ConditionProfile arityProfile,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Exclusive @Cached GilNode gil) throws ArityException {
            boolean mustRelease = gil.acquire();
            CApiTiming.enter();
            try {
                if (arityProfile.profile(arguments.length != 3)) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw ArityException.create(3, 3, arguments.length);
                }
                try {
                    callTernaryMethodNode.execute(null, getDelegate(), toJavaNode.execute(arguments[0]), toJavaNode.execute(arguments[1]), toJavaNode.execute(arguments[2]));
                    return 0;
                } catch (Throwable t) {
                    throw checkThrowableBeforeNative(t, "SetAttrWrapper", getDelegate());
                }
            } catch (PException e) {
                transformExceptionToNativeNode.execute(null, e);
                return -1;
            } finally {
                CApiTiming.exit(timing);
                gil.release(mustRelease);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,POINTER,POINTER):SINT32";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class InitWrapper extends PyProcsWrapper {

        public InitWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage(name = "execute")
        static class Execute {

            @Specialization(guards = "arguments.length == 3")
            static int init(InitWrapper self, Object[] arguments,
                            @Cached ExecutePositionalStarargsNode posStarargsNode,
                            @Cached ExpandKeywordStarargsNode expandKwargsNode,
                            @Cached CallVarargsMethodNode callNode,
                            @Cached NativeToPythonNode toJavaNode,
                            @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                            @Exclusive @Cached GilNode gil) {
                boolean mustRelease = gil.acquire();
                CApiTiming.enter();
                try {
                    try {
                        // convert args
                        Object receiver = toJavaNode.execute(arguments[0]);
                        Object starArgs = toJavaNode.execute(arguments[1]);
                        Object kwArgs = toJavaNode.execute(arguments[2]);

                        Object[] starArgsArray = posStarargsNode.executeWith(null, starArgs);
                        Object[] pArgs = PythonUtils.prependArgument(receiver, starArgsArray);
                        PKeyword[] kwArgsArray = expandKwargsNode.execute(kwArgs);
                        callNode.execute(null, self.getDelegate(), pArgs, kwArgsArray);
                        return 0;
                    } catch (Throwable t) {
                        throw checkThrowableBeforeNative(t, "InitWrapper", self.getDelegate());
                    }
                } catch (PException e) {
                    transformExceptionToNativeNode.execute(null, e);
                    return -1;
                } finally {
                    CApiTiming.exit(self.timing);
                    gil.release(mustRelease);
                }
            }

            @Specialization(guards = "arguments.length != 3")
            static int error(@SuppressWarnings("unused") InitWrapper self, Object[] arguments) throws ArityException {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw ArityException.create(3, 3, arguments.length);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,POINTER,POINTER):SINT32";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class VarargWrapper extends PyProcsWrapper {

        public VarargWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage(name = "execute")
        static class Execute {

            @Specialization(guards = "arguments.length == 2")
            static Object init(VarargWrapper self, Object[] arguments,
                            @Cached PythonToNativeTransferNode PythonToNativeTransferNode,
                            @Cached ExecutePositionalStarargsNode posStarargsNode,
                            @Cached CallVarargsMethodNode callNode,
                            @Cached NativeToPythonNode toJavaNode,
                            @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                            @Exclusive @Cached GilNode gil) {
                boolean mustRelease = gil.acquire();
                CApiTiming.enter();
                try {
                    try {
                        // convert args
                        Object receiver = toJavaNode.execute(arguments[0]);
                        Object starArgs = toJavaNode.execute(arguments[1]);

                        Object[] starArgsArray = posStarargsNode.executeWith(null, starArgs);
                        Object[] pArgs = PythonUtils.prependArgument(receiver, starArgsArray);
                        return PythonToNativeTransferNode.execute(callNode.execute(null, self.getDelegate(), pArgs, PKeyword.EMPTY_KEYWORDS));
                    } catch (Throwable t) {
                        throw checkThrowableBeforeNative(t, "VarargWrapper", self.getDelegate());
                    }
                } catch (PException e) {
                    transformExceptionToNativeNode.execute(null, e);
                    return PythonContext.get(gil).getNativeNull().getPtr();
                } finally {
                    CApiTiming.exit(self.timing);
                    gil.release(mustRelease);
                }
            }

            @Specialization(guards = "arguments.length != 2")
            static int error(@SuppressWarnings("unused") VarargWrapper self, Object[] arguments) throws ArityException {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw ArityException.create(2, 2, arguments.length);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,POINTER):POINTER";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class VarargKeywordWrapper extends PyProcsWrapper {

        public VarargKeywordWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage(name = "execute")
        static class Execute {

            @Specialization(guards = "arguments.length == 3")
            static Object init(VarargKeywordWrapper self, Object[] arguments,
                            @Cached PythonToNativeTransferNode PythonToNativeTransferNode,
                            @Cached ExecutePositionalStarargsNode posStarargsNode,
                            @Cached ExpandKeywordStarargsNode expandKwargsNode,
                            @Cached CallVarargsMethodNode callNode,
                            @Cached NativeToPythonNode toJavaNode,
                            @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                            @Exclusive @Cached GilNode gil) {
                boolean mustRelease = gil.acquire();
                CApiTiming.enter();
                try {
                    try {
                        // convert args
                        Object receiver = toJavaNode.execute(arguments[0]);
                        Object starArgs = toJavaNode.execute(arguments[1]);
                        Object kwArgs = toJavaNode.execute(arguments[2]);

                        Object[] starArgsArray = posStarargsNode.executeWith(null, starArgs);
                        Object[] pArgs = PythonUtils.prependArgument(receiver, starArgsArray);
                        PKeyword[] kwArgsArray = expandKwargsNode.execute(kwArgs);
                        return PythonToNativeTransferNode.execute(callNode.execute(null, self.getDelegate(), pArgs, kwArgsArray));
                    } catch (Throwable t) {
                        throw checkThrowableBeforeNative(t, "VarargKeywordWrapper", self.getDelegate());
                    }
                } catch (PException e) {
                    transformExceptionToNativeNode.execute(null, e);
                    return PythonContext.get(gil).getNativeNull().getPtr();
                } finally {
                    CApiTiming.exit(self.timing);
                    gil.release(mustRelease);
                }
            }

            @Specialization(guards = "arguments.length != 3")
            static int error(@SuppressWarnings("unused") VarargKeywordWrapper self, Object[] arguments) throws ArityException {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw ArityException.create(3, 3, arguments.length);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,POINTER,POINTER):POINTER";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class TernaryFunctionWrapper extends PyProcsWrapper {

        public TernaryFunctionWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage(name = "execute")
        static class Execute {

            @Specialization(guards = "arguments.length == 3")
            static Object call(TernaryFunctionWrapper self, Object[] arguments,
                            @Cached ExecutePositionalStarargsNode posStarargsNode,
                            @Cached ExpandKeywordStarargsNode expandKwargsNode,
                            @Cached CallVarargsMethodNode callNode,
                            @Cached NativeToPythonNode toJavaNode,
                            @Cached PythonToNativeTransferNode PythonToNativeTransferNode,
                            @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                            @Exclusive @Cached GilNode gil) {
                boolean mustRelease = gil.acquire();
                CApiTiming.enter();
                try {
                    try {
                        // convert args
                        Object receiver = toJavaNode.execute(arguments[0]);
                        Object starArgs = toJavaNode.execute(arguments[1]);
                        Object kwArgs = toJavaNode.execute(arguments[2]);

                        Object[] starArgsArray = posStarargsNode.executeWith(null, starArgs);
                        Object[] pArgs = PythonUtils.prependArgument(receiver, starArgsArray);
                        PKeyword[] kwArgsArray = expandKwargsNode.execute(kwArgs);
                        Object result = callNode.execute(null, self.getDelegate(), pArgs, kwArgsArray);
                        return PythonToNativeTransferNode.execute(result);
                    } catch (Throwable t) {
                        throw checkThrowableBeforeNative(t, "TernaryFunctionWrapper", self.getDelegate());
                    }
                } catch (PException e) {
                    transformExceptionToNativeNode.execute(null, e);
                    return PythonContext.get(gil).getNativeNull().getPtr();
                } finally {
                    CApiTiming.exit(self.timing);
                    gil.release(mustRelease);
                }
            }

            @Specialization(guards = "arguments.length != 3")
            static Object error(@SuppressWarnings("unused") TernaryFunctionWrapper self, Object[] arguments) throws ArityException {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw ArityException.create(3, 3, arguments.length);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,POINTER,POINTER):POINTER";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class RichcmpFunctionWrapper extends PyProcsWrapper {

        public RichcmpFunctionWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                        @Cached NativeToPythonNode toJavaNode,
                        @Cached CallTernaryMethodNode callNode,
                        @Cached PythonToNativeTransferNode PythonToNativeTransferNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Exclusive @Cached GilNode gil) throws ArityException {
            boolean mustRelease = gil.acquire();
            CApiTiming.enter();
            try {
                if (arguments.length != 3) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw ArityException.create(3, 3, arguments.length);
                }
                try {
                    // convert args
                    Object arg0 = toJavaNode.execute(arguments[0]);
                    Object arg1 = toJavaNode.execute(arguments[1]);
                    Object arg2 = arguments[2];

                    Object result = callNode.execute(null, getDelegate(), arg0, arg1, arg2);
                    return PythonToNativeTransferNode.execute(result);
                } catch (Throwable t) {
                    throw checkThrowableBeforeNative(t, "RichcmpFunctionWrapper", getDelegate());
                }
            } catch (PException e) {
                transformExceptionToNativeNode.execute(null, e);
                return PythonContext.get(gil).getNativeNull().getPtr();
            } finally {
                CApiTiming.exit(timing);
                gil.release(mustRelease);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,POINTER,SINT32):POINTER";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class SsizeargfuncWrapper extends PyProcsWrapper {

        public SsizeargfuncWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                        @Cached PythonToNativeTransferNode PythonToNativeTransferNode,
                        @Cached CallBinaryMethodNode executeNode,
                        @Cached NativeToPythonNode toJavaNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Exclusive @Cached GilNode gil) throws ArityException {
            boolean mustRelease = gil.acquire();
            CApiTiming.enter();
            try {
                if (arguments.length != 2) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw ArityException.create(2, 2, arguments.length);
                }
                assert arguments[1] instanceof Number;
                try {
                    Object result = executeNode.executeObject(null, getDelegate(), toJavaNode.execute(arguments[0]), arguments[1]);
                    return PythonToNativeTransferNode.execute(result);
                } catch (Throwable t) {
                    throw checkThrowableBeforeNative(t, "SsizeargfuncWrapper", getDelegate());
                }
            } catch (PException e) {
                transformExceptionToNativeNode.execute(null, e);
                return PythonContext.get(toJavaNode).getNativeNull().getPtr();
            } finally {
                CApiTiming.exit(timing);
                gil.release(mustRelease);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,SINT64):POINTER";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class LenfuncWrapper extends PyProcsWrapper {

        public LenfuncWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                        @Cached CallUnaryMethodNode executeNode,
                        @Cached NativeToPythonNode toJavaNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Cached PyNumberIndexNode indexNode,
                        @Cached CastToJavaIntLossyNode castLossy,
                        @Cached PyNumberAsSizeNode asSizeNode,
                        @Cached PRaiseNode raiseNode,
                        @Exclusive @Cached GilNode gil) throws ArityException {
            boolean mustRelease = gil.acquire();
            CApiTiming.enter();
            try {
                if (arguments.length != 1) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw ArityException.create(1, 1, arguments.length);
                }
                try {
                    Object result = executeNode.executeObject(null, getDelegate(), toJavaNode.execute(arguments[0]));
                    int len = PyObjectSizeNode.convertAndCheckLen(null, result, indexNode, castLossy, asSizeNode, raiseNode);
                    return (long) len;
                } catch (Throwable t) {
                    throw checkThrowableBeforeNative(t, "LenfuncWrapper", getDelegate());
                }
            } catch (PException e) {
                transformExceptionToNativeNode.execute(null, e);
                return PythonContext.get(toJavaNode).getNativeNull().getPtr();
            } finally {
                CApiTiming.exit(timing);
                gil.release(mustRelease);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER):SINT64";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class HashfuncWrapper extends PyProcsWrapper {

        public HashfuncWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                        @Cached CallUnaryMethodNode executeNode,
                        @Cached NativeToPythonNode toJavaNode,
                        @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                        @Exclusive @Cached GilNode gil) throws ArityException {
            boolean mustRelease = gil.acquire();
            CApiTiming.enter();
            try {
                /*
                 * Accept a second argumenthere, since these functions are sometimes called using
                 * METH_O with a "NULL" value.
                 */
                if (arguments.length > 2) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    throw ArityException.create(1, 2, arguments.length);
                }
                try {
                    return executeNode.executeObject(null, getDelegate(), toJavaNode.execute(arguments[0]));
                } catch (Throwable t) {
                    throw checkThrowableBeforeNative(t, "HashfuncWrapper", getDelegate());
                }
            } catch (PException e) {
                transformExceptionToNativeNode.execute(null, e);
                return PythonContext.get(gil).getNativeNull().getPtr();
            } finally {
                CApiTiming.exit(timing);
                gil.release(mustRelease);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER):SINT64";
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class DescrGetFunctionWrapper extends PyProcsWrapper {

        public DescrGetFunctionWrapper(Object delegate) {
            super(delegate);
        }

        @ExportMessage(name = "execute")
        static class Execute {

            @Specialization(guards = "arguments.length == 3")
            static Object call(DescrGetFunctionWrapper self, Object[] arguments,
                            @Cached CallTernaryMethodNode callNode,
                            @Cached NativeToPythonNode toJavaNode,
                            @Cached PythonToNativeTransferNode PythonToNativeTransferNode,
                            @Cached TransformExceptionToNativeNode transformExceptionToNativeNode,
                            @Exclusive @Cached GilNode gil) {
                boolean mustRelease = gil.acquire();
                CApiTiming.enter();
                try {
                    try {
                        // convert args
                        Object receiver = toJavaNode.execute(arguments[0]);
                        Object obj = toJavaNode.execute(arguments[1]);
                        Object cls = toJavaNode.execute(arguments[2]);

                        Object result = callNode.execute(null, self.getDelegate(), receiver, obj, cls);
                        return PythonToNativeTransferNode.execute(result);
                    } catch (Throwable t) {
                        throw checkThrowableBeforeNative(t, "DescrGetFunctionWrapper", self.getDelegate());
                    }
                } catch (PException e) {
                    transformExceptionToNativeNode.execute(null, e);
                    return PythonContext.get(gil).getNativeNull().getPtr();
                } finally {
                    CApiTiming.exit(self.timing);
                    gil.release(mustRelease);
                }
            }

            @Specialization(guards = "arguments.length != 3")
            static Object error(@SuppressWarnings("unused") DescrGetFunctionWrapper self, Object[] arguments) throws ArityException {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw ArityException.create(3, 3, arguments.length);
            }
        }

        @Override
        protected String getSignature() {
            return "(POINTER,POINTER,POINTER):POINTER";
        }
    }

    public static GetAttrWrapper createGetAttrWrapper(Object method) {
        assert !(method instanceof PNone) && !(method instanceof PNotImplemented);
        return new GetAttrWrapper(method);
    }

    public static UnaryFuncWrapper createUnaryFuncWrapper(Object method) {
        assert !(method instanceof PNone) && !(method instanceof PNotImplemented);
        return new UnaryFuncWrapper(method);
    }

    public static BinaryFuncWrapper createBinaryFuncWrapper(Object method) {
        assert !(method instanceof PNone) && !(method instanceof PNotImplemented);
        return new BinaryFuncWrapper(method);
    }

    public static SetAttrWrapper createSetAttrWrapper(Object setAttrMethod) {
        assert !(setAttrMethod instanceof PNone) && !(setAttrMethod instanceof PNotImplemented);
        return new SetAttrWrapper(setAttrMethod);
    }

    public static InitWrapper createInitWrapper(Object setInitMethod) {
        assert !(setInitMethod instanceof PNone) && !(setInitMethod instanceof PNotImplemented);
        return new InitWrapper(setInitMethod);
    }

    public static VarargWrapper createVarargWrapper(Object method) {
        assert !(method instanceof PNone) && !(method instanceof PNotImplemented);
        return new VarargWrapper(method);
    }

    public static VarargKeywordWrapper createVarargKeywordWrapper(Object method) {
        assert !(method instanceof PNone) && !(method instanceof PNotImplemented);
        return new VarargKeywordWrapper(method);
    }

    /**
     * Wraps CPython's {@code ternaryfunc} slots.
     */
    public static TernaryFunctionWrapper createTernaryFunctionWrapper(Object method) {
        assert !(method instanceof PNone) && !(method instanceof PNotImplemented);
        return new TernaryFunctionWrapper(method);
    }

    public static SsizeargfuncWrapper createSsizeargfuncWrapper(Object method) {
        assert !(method instanceof PNone) && !(method instanceof PNotImplemented);
        return new SsizeargfuncWrapper(method);
    }

    public static LenfuncWrapper createLenfuncWrapper(Object method) {
        assert !(method instanceof PNone) && !(method instanceof PNotImplemented);
        return new LenfuncWrapper(method);
    }
}
