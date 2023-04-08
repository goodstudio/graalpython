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

import static com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiCallPath.CImpl;
import static com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiCallPath.Ignored;
import static com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiCallPath.NotImplemented;
import static com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiCallPath.PolyglotImpl;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CHAR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CHAR_CONST_ARRAY;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CHAR_CONST_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CHAR_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CHAR_PTR_LIST;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_CHAR_PTR_LIST;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_PYCONFIG_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_PYPRECONFIG_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_PY_BUFFER;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_PY_SSIZE_T;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_PY_SSIZE_T_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_PY_UCS4;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_PY_UNICODE;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_UNSIGNED_CHAR_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_VOID_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_VOID_PTR_LIST;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CONST_WCHAR_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.CROSSINTERPDATAFUNC;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.ConstCharPtr;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.ConstCharPtrAsTruffleString;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.ConstInt;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.ConstPyObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.ConstPyVarObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.Double;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.FILE_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.FREEFUNC;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.INITTAB;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.INT64_T;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.INT_LIST;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.Int;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.LONG_LONG;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.LONG_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.Long;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYCONFIG_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYDICTKEYSOBJECT_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYDICTOBJECT_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYHASH_FUNCDEF_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYMEMALLOCATORDOMAIN;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYMEMALLOCATOREX_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYMODULEDEF_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYOBJECTARENAALLOCATOR_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYPRECONFIG_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYSTATUS;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYUNICODE_KIND;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYWEAKREFERENCE_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PYWIDESTRINGLIST_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_AUDITHOOKFUNCTION;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_BUFFER;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_COMPILER_FLAGS;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_COMPLEX;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_C_FUNCTION;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_GEN_OBJECT;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_HASH_T_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_IDENTIFIER;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_LOCK_STATUS;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_OPENCODEHOOKFUNCTION;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_OS_SIGHANDLER;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_SSIZE_T_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_STRUCT_SEQUENCE_DESC;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_THREAD_TYPE_LOCK;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_THREAD_TYPE_LOCK_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_TRACEFUNC;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_TSS_T_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_TYPE_SPEC;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_UCS4;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_UCS4_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PY_UNICODE_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.Pointer;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyASCIIObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyCodeAddressRange;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyCodeObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyFrameConstructor;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyFrameObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyFrameObjectTransfer;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyGetSetDef;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyInterpreterState;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyLongObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyMemberDef;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyMethodDef;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyModuleDef;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyObjectBorrowed;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyObjectConstPtr;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyObjectPtr;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyObjectTransfer;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PySendResult;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PySliceObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyThreadState;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyTryBlock;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyTypeObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyUnicodeObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.PyVarObject;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.Py_hash_t;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.Py_ssize_t;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.SIZE_T;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.SIZE_T_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.STAT_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.TIMESPEC_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.TIMEVAL_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.TIME_T;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.TIME_T_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.TM_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.TS_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.UINT64_T;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.UINTPTR_T;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.UNSIGNED_CHAR_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.UNSIGNED_INT;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.UNSIGNED_LONG;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.UNSIGNED_LONG_LONG;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.VARARGS;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.VA_LIST;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.VOID_PTR_LIST;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.Void;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.VoidNoReturn;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.WCHAR_T_CONST_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.WCHAR_T_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.WCHAR_T_PTR_LIST;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.WCHAR_T_PTR_PTR_LIST;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.WRAPPERBASE;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PYARG_PARSER_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PYBYTESWRITER_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PYCROSSINTERPRETERDATA_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PYERR_STACKITEM_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PYTIME_ROUND_T;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PYTIME_T;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PYTIME_T_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PYUNICODEWRITER_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PY_CLOCK_INFO_T_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PY_ERROR_HANDLER;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PY_IDENTIFIER_PTR;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor._PyFrameEvalFunction;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.destructor;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.func_intvoidptr;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.func_objcharsizevoidptr;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.func_objint;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.func_objvoid;
import static com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor.func_voidvoidptr;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.oracle.graal.python.builtins.modules.cext.PythonCextAbstractBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBoolBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiBuiltin;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiBuiltinNode;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBuiltins.CApiBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextBytesBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextCEvalBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextCapsuleBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextClassBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextCodeBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextComplexBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextContextBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextDateTimeBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextDescrBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextDictBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextErrBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextFileBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextFloatBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextFuncBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextGenericAliasBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextHashBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextImportBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextIterBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextListBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextLongBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextMemoryViewBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextMethodBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextModuleBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextNamespaceBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextObjectBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextPosixmoduleBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextPyLifecycleBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextPyStateBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextPyThreadBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextPythonRunBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextSetBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextSliceBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextSlotBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextStructSeqBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextSysBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextTracebackBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextTupleBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextTypeBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextUnicodeBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextWarnBuiltins;
import com.oracle.graal.python.builtins.modules.cext.PythonCextWeakrefBuiltins;
import com.oracle.graal.python.builtins.objects.cext.capi.CApiCodeGen.CApiBuiltinDesc;
import com.oracle.graal.python.builtins.objects.cext.capi.transitions.ArgDescriptor;

/**
 * This file contains the specification of all CAPI builtins that aren't explicitly implemented in
 * {@link PythonCextBuiltins}, etc. I.e., all builtins that are implemented in C code or that are
 * not implemented at all at the moment.
 */
public final class CApiFunction {

    /*
     * Functions that are implemented as C code that can be executed both in native and in Sulong:
     */
    @CApiBuiltin(name = "_Py_c_abs", ret = Double, args = {PY_COMPLEX}, call = CImpl)
    @CApiBuiltin(name = "_Py_c_diff", ret = PY_COMPLEX, args = {PY_COMPLEX, PY_COMPLEX}, call = CImpl)
    @CApiBuiltin(name = "_Py_c_neg", ret = PY_COMPLEX, args = {PY_COMPLEX}, call = CImpl)
    @CApiBuiltin(name = "_Py_c_pow", ret = PY_COMPLEX, args = {PY_COMPLEX, PY_COMPLEX}, call = CImpl)
    @CApiBuiltin(name = "_Py_c_prod", ret = PY_COMPLEX, args = {PY_COMPLEX, PY_COMPLEX}, call = CImpl)
    @CApiBuiltin(name = "_Py_c_quot", ret = PY_COMPLEX, args = {PY_COMPLEX, PY_COMPLEX}, call = CImpl)
    @CApiBuiltin(name = "_Py_c_sum", ret = PY_COMPLEX, args = {PY_COMPLEX, PY_COMPLEX}, call = CImpl)
    @CApiBuiltin(name = "_Py_BuildValue_SizeT", ret = PyObject, args = {ConstCharPtrAsTruffleString, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "_Py_REFCNT", ret = Py_ssize_t, args = {ConstPyObject}, call = CImpl)
    @CApiBuiltin(name = "_Py_SET_REFCNT", ret = Py_ssize_t, args = {PyObject, Py_ssize_t}, call = CImpl)
    @CApiBuiltin(name = "_Py_SET_SIZE", ret = Void, args = {PyVarObject, Py_ssize_t}, call = CImpl)
    @CApiBuiltin(name = "_Py_SET_TYPE", ret = Void, args = {PyObject, PyTypeObject}, call = CImpl)
    @CApiBuiltin(name = "_Py_SIZE", ret = Py_ssize_t, args = {ConstPyVarObject}, call = CImpl)
    @CApiBuiltin(name = "_Py_TYPE", ret = PyTypeObject, args = {ConstPyObject}, call = CImpl)
    @CApiBuiltin(name = "_Py_VaBuildStack_SizeT", ret = PyObjectPtr, args = {PyObjectPtr, Py_ssize_t, ConstCharPtrAsTruffleString, VA_LIST, PY_SSIZE_T_PTR}, call = CImpl)
    @CApiBuiltin(name = "_Py_VaBuildStack", ret = PyObjectPtr, args = {PyObjectPtr, Py_ssize_t, ConstCharPtrAsTruffleString, VA_LIST, PY_SSIZE_T_PTR}, call = CImpl)
    @CApiBuiltin(name = "_Py_VaBuildValue_SizeT", ret = PyObject, args = {ConstCharPtrAsTruffleString, VA_LIST}, call = CImpl)
    @CApiBuiltin(name = "_PyArg_CheckPositional", ret = Int, args = {ConstCharPtrAsTruffleString, Py_ssize_t, Py_ssize_t, Py_ssize_t}, call = CImpl)
    @CApiBuiltin(name = "_PyArg_BadArgument", ret = Void, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, PyObject}, call = CImpl)
    @CApiBuiltin(name = "_PyArg_NoKeywords", ret = Int, args = {ConstCharPtrAsTruffleString, PyObject}, call = CImpl)
    @CApiBuiltin(name = "_PyArg_NoPositional", ret = Int, args = {ConstCharPtrAsTruffleString, PyObject}, call = CImpl)
    @CApiBuiltin(name = "_PyArg_UnpackKeywords", ret = PyObjectConstPtr, args = {PyObjectConstPtr, Py_ssize_t, PyObject, PyObject, _PYARG_PARSER_PTR, Int, Int, Int, PyObjectPtr}, call = CImpl)
    @CApiBuiltin(name = "_PyArg_UnpackStack", ret = Int, args = {PyObjectConstPtr, Py_ssize_t, ConstCharPtrAsTruffleString, Py_ssize_t, Py_ssize_t, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "_PyDict_GetItemIdWithError", ret = PyObject, args = {PyObject, _PY_IDENTIFIER_PTR}, call = CImpl)
    @CApiBuiltin(name = "_PyDict_GetItemStringWithError", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString}, call = CImpl)
    @CApiBuiltin(name = "_PyDict_NewPresized", ret = PyObject, args = {Py_ssize_t}, call = CImpl)
    @CApiBuiltin(name = "_PyDict_ContainsId", ret = Int, args = {PyObject, _PY_IDENTIFIER_PTR}, call = CImpl)
    @CApiBuiltin(name = "_PyDict_SetItemId", ret = Int, args = {PyObject, _PY_IDENTIFIER_PTR, PyObject}, call = CImpl)
    @CApiBuiltin(name = "_PyDict_Next", ret = Int, args = {PyObject, PY_SSIZE_T_PTR, PyObjectPtr, PyObjectPtr, PY_HASH_T_PTR}, call = CImpl)
    @CApiBuiltin(name = "_PyObject_GetDictPtr", ret = PyObjectPtr, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "_PyDict_GetItem_KnownHash", ret = PyObject, args = {PyObject, PyObject, Py_hash_t}, call = CImpl)
    @CApiBuiltin(name = "_PyObject_GC_Calloc", ret = PyObject, args = {SIZE_T}, call = CImpl)
    @CApiBuiltin(name = "_PyObject_GC_Malloc", ret = PyObject, args = {SIZE_T}, call = CImpl)
    @CApiBuiltin(name = "Py_BuildValue", ret = PyObject, args = {ConstCharPtrAsTruffleString, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "Py_Is", ret = Int, args = {PyObject, PyObject}, call = CImpl)
    @CApiBuiltin(name = "Py_IsFalse", ret = Int, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "Py_IsNone", ret = Int, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "Py_IsTrue", ret = Int, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "Py_VaBuildValue", ret = PyObject, args = {ConstCharPtrAsTruffleString, VA_LIST}, call = CImpl)
    @CApiBuiltin(name = "PyArg_UnpackTuple", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, Py_ssize_t, Py_ssize_t, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "PyBool_FromLong", ret = PyObject, args = {Long}, call = CImpl)
    @CApiBuiltin(name = "PyComplex_AsCComplex", ret = PY_COMPLEX, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyComplex_FromCComplex", ret = PyObject, args = {PY_COMPLEX}, call = CImpl)
    @CApiBuiltin(name = "PyDict_DelItemString", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString}, call = CImpl)
    @CApiBuiltin(name = "PyDict_GetItemString", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString}, call = CImpl)
    @CApiBuiltin(name = "PyDict_SetItemString", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyDict_Next", ret = Int, args = {PyObject, PY_SSIZE_T_PTR, PyObjectPtr, PyObjectPtr}, call = CImpl)
    @CApiBuiltin(name = "PyErr_ResourceWarning", ret = Int, args = {PyObject, Py_ssize_t, ConstCharPtrAsTruffleString, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "PyErr_WarnEx", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, Py_ssize_t}, call = CImpl)
    @CApiBuiltin(name = "PyErr_WarnFormat", ret = Int, args = {PyObject, Py_ssize_t, ConstCharPtrAsTruffleString, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "PyMem_Calloc", ret = Pointer, args = {SIZE_T, SIZE_T}, call = CImpl)
    @CApiBuiltin(name = "PyMem_Free", ret = Void, args = {Pointer}, call = CImpl)
    @CApiBuiltin(name = "PyMem_Malloc", ret = Pointer, args = {SIZE_T}, call = CImpl)
    @CApiBuiltin(name = "PyMem_RawCalloc", ret = Pointer, args = {SIZE_T, SIZE_T}, call = CImpl)
    @CApiBuiltin(name = "PyMem_RawFree", ret = Void, args = {Pointer}, call = CImpl)
    @CApiBuiltin(name = "PyMem_RawMalloc", ret = Pointer, args = {SIZE_T}, call = CImpl)
    @CApiBuiltin(name = "PyMem_RawRealloc", ret = Pointer, args = {Pointer, SIZE_T}, call = CImpl)
    @CApiBuiltin(name = "PyMem_Realloc", ret = Pointer, args = {Pointer, SIZE_T}, call = CImpl)
    @CApiBuiltin(name = "PyModule_AddObject", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyModule_AddStringConstant", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = CImpl)
    @CApiBuiltin(name = "PyModule_AddType", ret = Int, args = {PyObject, PyTypeObject}, call = CImpl)
    @CApiBuiltin(name = "PyObject_GenericGetDict", ret = PyObject, args = {PyObject, Pointer}, call = CImpl)
    @CApiBuiltin(name = "Py_IsInitialized", ret = Int, args = {}, call = CImpl)
    @CApiBuiltin(name = "PyObject_Free", ret = Void, args = {Pointer}, call = CImpl)
    @CApiBuiltin(name = "PyObject_Malloc", ret = Pointer, args = {SIZE_T}, call = CImpl)
    @CApiBuiltin(name = "PyObject_Realloc", ret = Pointer, args = {Pointer, SIZE_T}, call = CImpl)

    @CApiBuiltin(name = "_Py_DecRef", ret = Void, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "_Py_IncRef", ret = Void, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "Py_DecRef", ret = Void, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "Py_IncRef", ret = Void, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "_Py_Dealloc", ret = Void, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "_Py_NewReference", ret = Void, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyObject_GC_Del", ret = Void, args = {Pointer}, call = CImpl)
    @CApiBuiltin(name = "PyObject_Init", ret = PyObject, args = {PyObject, PyTypeObject}, call = CImpl)
    @CApiBuiltin(name = "PyObject_InitVar", ret = PyVarObject, args = {PyVarObject, PyTypeObject, Py_ssize_t}, call = CImpl)
    @CApiBuiltin(name = "_PyObject_GC_New", ret = PyObject, args = {PyTypeObject}, call = CImpl)
    @CApiBuiltin(name = "_PyObject_GC_NewVar", ret = PyVarObject, args = {PyTypeObject, Py_ssize_t}, call = CImpl)
    @CApiBuiltin(name = "_PyObject_New", ret = PyObject, args = {PyTypeObject}, call = CImpl)
    @CApiBuiltin(name = "_PyObject_NewVar", ret = PyVarObject, args = {PyTypeObject, Py_ssize_t}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsDouble", ret = Double, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsLong", ret = Long, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsLongAndOverflow", ret = Long, args = {PyObject, INT_LIST}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsLongLong", ret = LONG_LONG, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsLongLongAndOverflow", ret = LONG_LONG, args = {PyObject, INT_LIST}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsSize_t", ret = SIZE_T, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsSsize_t", ret = Py_ssize_t, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsUnsignedLong", ret = UNSIGNED_LONG, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsUnsignedLongLong", ret = UNSIGNED_LONG_LONG, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsUnsignedLongLongMask", ret = UNSIGNED_LONG_LONG, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyLong_AsUnsignedLongMask", ret = UNSIGNED_LONG, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyLong_FromVoidPtr", ret = PyObject, args = {Pointer}, call = CImpl)
    @CApiBuiltin(name = "_PyLong_AsByteArray", ret = Int, args = {PyLongObject, UNSIGNED_CHAR_PTR, SIZE_T, Int, Int}, call = CImpl)
    @CApiBuiltin(name = "_PyLong_AsInt", ret = Int, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "_PyFloat_Pack2", ret = Int, args = {Double, UNSIGNED_CHAR_PTR, Int}, call = CImpl)
    @CApiBuiltin(name = "_PyFloat_Pack4", ret = Int, args = {Double, UNSIGNED_CHAR_PTR, Int}, call = CImpl)
    @CApiBuiltin(name = "_PyFloat_Pack8", ret = Int, args = {Double, UNSIGNED_CHAR_PTR, Int}, call = CImpl)
    @CApiBuiltin(name = "_PyFloat_Unpack2", ret = Double, args = {CONST_UNSIGNED_CHAR_PTR, Int}, call = CImpl)
    @CApiBuiltin(name = "_PyFloat_Unpack4", ret = Double, args = {CONST_UNSIGNED_CHAR_PTR, Int}, call = CImpl)
    @CApiBuiltin(name = "_PyFloat_Unpack8", ret = Double, args = {CONST_UNSIGNED_CHAR_PTR, Int}, call = CImpl)
    @CApiBuiltin(name = "PyFloat_AsDouble", ret = ArgDescriptor.Double, args = {PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyType_GetFlags", ret = UNSIGNED_LONG, args = {PyTypeObject}, call = CImpl)
    @CApiBuiltin(name = "PySys_Audit", ret = Int, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "PyOS_mystricmp", ret = Int, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = CImpl)
    @CApiBuiltin(name = "PyOS_mystrnicmp", ret = Int, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, Py_ssize_t}, call = CImpl)
    @CApiBuiltin(name = "PyObject_Call", ret = PyObject, args = {PyObject, PyObject, PyObject}, call = CImpl)
    @CApiBuiltin(name = "PyObject_CallObject", ret = PyObject, args = {PyObject, PyObject}, call = CImpl)
    @CApiBuiltin(name = "_PyObject_CallFunction_SizeT", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "PyObject_CallFunction", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "_PyObject_CallMethod_SizeT", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "PyObject_CallMethod", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, VARARGS}, call = CImpl)
    @CApiBuiltin(name = "PyDescr_IsData", ret = Int, args = {PyObject}, call = CImpl)

    /*
     * Functions that are implemented in C code that needs to run on Sulong:
     */
    @CApiBuiltin(name = "_PyArg_ParseStack_SizeT", ret = Int, args = {PyObjectConstPtr, Py_ssize_t, ConstCharPtrAsTruffleString, VARARGS}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyArg_ParseTupleAndKeywordsFast_SizeT", ret = Int, args = {PyObject, PyObject, _PYARG_PARSER_PTR,
                    VARARGS}, forwardsTo = "_PyArg_VaParseTupleAndKeywordsFast_SizeT", call = PolyglotImpl)
    @CApiBuiltin(name = "_PyArg_VaParseTupleAndKeywordsFast_SizeT", ret = Int, args = {PyObject, PyObject, _PYARG_PARSER_PTR, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyArg_VaParse_SizeT", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_FatalErrorFunc", ret = VoidNoReturn, args = {ConstCharPtr, ConstCharPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_gitidentifier", ret = ConstCharPtrAsTruffleString, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_gitversion", ret = ConstCharPtrAsTruffleString, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_HashBytes", ret = Py_hash_t, args = {CONST_VOID_PTR, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_HashPointer", ret = Py_hash_t, args = {CONST_VOID_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_HashPointerRaw", ret = Py_hash_t, args = {CONST_VOID_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_parse_inf_or_nan", ret = Double, args = {ConstCharPtrAsTruffleString, CHAR_PTR_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_strhex_bytes_with_sep", ret = PyObject, args = {ConstCharPtrAsTruffleString, CONST_PY_SSIZE_T, ConstPyObject, ConstInt}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_strhex_bytes", ret = PyObject, args = {ConstCharPtrAsTruffleString, CONST_PY_SSIZE_T}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_strhex_with_sep", ret = PyObject, args = {ConstCharPtrAsTruffleString, CONST_PY_SSIZE_T, ConstPyObject, ConstInt}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_strhex", ret = PyObject, args = {ConstCharPtrAsTruffleString, CONST_PY_SSIZE_T}, call = PolyglotImpl)
    @CApiBuiltin(name = "_Py_string_to_number_with_underscores", ret = PyObject, args = {ConstCharPtr, Py_ssize_t, ConstCharPtr, PyObject, Pointer, func_objcharsizevoidptr}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyArg_Parse_SizeT", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, VARARGS}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyArg_ParseTuple_SizeT", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, VARARGS}, forwardsTo = "PyArg_VaParse", call = PolyglotImpl)
    @CApiBuiltin(name = "_PyArg_ParseTupleAndKeywords_SizeT", ret = Int, args = {PyObject, PyObject, ConstCharPtrAsTruffleString, CHAR_PTR_LIST,
                    VARARGS}, forwardsTo = "PyArg_VaParseTupleAndKeywords", call = PolyglotImpl)
    @CApiBuiltin(name = "_PyArg_ParseTupleAndKeywordsFast", ret = Int, args = {PyObject, PyObject, _PYARG_PARSER_PTR, VARARGS}, forwardsTo = "_PyArg_VaParseTupleAndKeywordsFast", call = PolyglotImpl)
    @CApiBuiltin(name = "_PyArg_VaParseTupleAndKeywords_SizeT", ret = Int, args = {PyObject, PyObject, ConstCharPtrAsTruffleString, CHAR_PTR_LIST, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyArg_VaParseTupleAndKeywordsFast", ret = Int, args = {PyObject, PyObject, _PYARG_PARSER_PTR, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyASCIIObject_LENGTH", ret = Py_ssize_t, args = {PyASCIIObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyASCIIObject_STATE_ASCII", ret = UNSIGNED_INT, args = {PyASCIIObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyASCIIObject_STATE_COMPACT", ret = UNSIGNED_INT, args = {PyASCIIObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyASCIIObject_STATE_KIND", ret = UNSIGNED_INT, args = {PyASCIIObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyASCIIObject_STATE_READY", ret = UNSIGNED_INT, args = {PyASCIIObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyASCIIObject_WSTR", ret = WCHAR_T_PTR, args = {PyASCIIObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyByteArray_Start", ret = CHAR_PTR, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyBytes_Resize", ret = Int, args = {PyObjectPtr, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyBytesWriter_Alloc", ret = Pointer, args = {_PYBYTESWRITER_PTR, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyBytesWriter_Dealloc", ret = Void, args = {_PYBYTESWRITER_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyBytesWriter_Finish", ret = PyObject, args = {_PYBYTESWRITER_PTR, Pointer}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyBytesWriter_Init", ret = Void, args = {_PYBYTESWRITER_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyBytesWriter_Prepare", ret = Pointer, args = {_PYBYTESWRITER_PTR, Pointer, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyBytesWriter_Resize", ret = Pointer, args = {_PYBYTESWRITER_PTR, Pointer, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyBytesWriter_WriteBytes", ret = Pointer, args = {_PYBYTESWRITER_PTR, Pointer, CONST_VOID_PTR, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyEval_SliceIndex", ret = Int, args = {PyObject, PY_SSIZE_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyFrame_SetLineNumber", ret = Void, args = {PyFrameObject, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyGen_FetchStopIterationValue", ret = Int, args = {PyObjectPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyGen_Finalize", ret = Void, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyGen_SetStopIterationValue", ret = Int, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyGen_yf", ret = PyObject, args = {PY_GEN_OBJECT}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyImport_SetModule", ret = Int, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyLong_FromTime_t", ret = PyObject, args = {TIME_T}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyMemoryView_GetBuffer", ret = PY_BUFFER, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyModule_CreateInitialized", ret = PyObject, args = {PYMODULEDEF_PTR, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyModule_GetDef", ret = PyModuleDef, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyModule_GetDict", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyModule_GetState", ret = Pointer, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyObject_GetAttrId", ret = PyObject, args = {PyObject, _PY_IDENTIFIER_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyObject_GetMethod", ret = Int, args = {PyObject, PyObject, PyObjectPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyObject_LookupAttr", ret = Int, args = {PyObject, PyObject, PyObjectPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyObject_LookupAttrId", ret = Int, args = {PyObject, _PY_IDENTIFIER_PTR, PyObjectPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyObject_MakeTpCall", ret = PyObject, args = {PyThreadState, PyObject, PyObjectConstPtr, Py_ssize_t, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyObject_NextNotImplemented", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyObject_SetAttrId", ret = Int, args = {PyObject, _PY_IDENTIFIER_PTR, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PySequence_Fast_ITEMS", ret = PyObjectPtr, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PySequence_ITEM", ret = PyObject, args = {PyObject, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PySet_NextEntry", ret = Int, args = {PyObject, PY_SSIZE_T_PTR, PyObjectPtr, PY_HASH_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyThreadState_UncheckedGet", ret = PyThreadState, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyType_GetModuleByDef", ret = PyObject, args = {PyTypeObject, PYMODULEDEF_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyType_Name", ret = ConstCharPtrAsTruffleString, args = {PyTypeObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_EqualToASCIIId", ret = Int, args = {PyObject, PY_IDENTIFIER}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_FromId", ret = PyObject, args = {PY_IDENTIFIER}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_get_wstr_length", ret = Py_ssize_t, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsAlpha", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsCased", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsCaseIgnorable", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsDecimalDigit", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsDigit", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsLinebreak", ret = Int, args = {CONST_PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsLowercase", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsNumeric", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsPrintable", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsTitlecase", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsUppercase", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsWhitespace", ret = Int, args = {CONST_PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsXidContinue", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_IsXidStart", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_Ready", ret = Int, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToDecimalDigit", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToDigit", ret = Int, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToFoldedFull", ret = Int, args = {PY_UCS4, PY_UCS4_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToLowercase", ret = PY_UCS4, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToLowerFull", ret = Int, args = {PY_UCS4, PY_UCS4_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToNumeric", ret = Double, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToTitlecase", ret = PY_UCS4, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToTitleFull", ret = Int, args = {PY_UCS4, PY_UCS4_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToUppercase", ret = PY_UCS4, args = {PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicode_ToUpperFull", ret = Int, args = {PY_UCS4, PY_UCS4_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyUnicodeObject_DATA", ret = Pointer, args = {PyUnicodeObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "Py_GetBuildInfo", ret = ConstCharPtrAsTruffleString, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "Py_GetCompiler", ret = ConstCharPtrAsTruffleString, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "Py_GetVersion", ret = ConstCharPtrAsTruffleString, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "Py_NewRef", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "Py_XNewRef", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyArg_Parse", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, VARARGS}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyArg_ParseTuple", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, VARARGS}, forwardsTo = "PyArg_VaParse", call = PolyglotImpl)
    @CApiBuiltin(name = "PyArg_ParseTupleAndKeywords", ret = Int, args = {PyObject, PyObject, ConstCharPtrAsTruffleString, CHAR_PTR_LIST,
                    VARARGS}, forwardsTo = "PyArg_VaParseTupleAndKeywords", call = PolyglotImpl)
    @CApiBuiltin(name = "PyArg_VaParse", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyArg_VaParseTupleAndKeywords", ret = Int, args = {PyObject, PyObject, ConstCharPtrAsTruffleString, CHAR_PTR_LIST, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBuffer_FillInfo", ret = Int, args = {PY_BUFFER, PyObject, Pointer, Py_ssize_t, Int, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBuffer_IsContiguous", ret = Int, args = {CONST_PY_BUFFER, CHAR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBuffer_Release", ret = Void, args = {PY_BUFFER}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyByteArray_FromStringAndSize", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBytes_AsString", ret = CHAR_PTR, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBytes_AsStringAndSize", ret = Int, args = {PyObject, CHAR_PTR_LIST, PY_SSIZE_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBytes_Concat", ret = Void, args = {PyObjectPtr, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBytes_ConcatAndDel", ret = Void, args = {PyObjectPtr, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBytes_FromFormat", ret = PyObject, args = {ConstCharPtrAsTruffleString, VARARGS}, forwardsTo = "PyBytes_FromFormatV", call = PolyglotImpl)
    @CApiBuiltin(name = "PyBytes_FromFormatV", ret = PyObject, args = {ConstCharPtrAsTruffleString, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBytes_FromString", ret = PyObject, args = {ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyBytes_FromStringAndSize", ret = PyObjectTransfer, args = {ConstCharPtr, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyCFunction_GetClass", ret = PyTypeObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyCFunction_GetFlags", ret = Int, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyCFunction_GetFunction", ret = PY_C_FUNCTION, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyCFunction_GetSelf", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyCFunction_New", ret = PyObject, args = {PyMethodDef, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyCFunction_NewEx", ret = PyObject, args = {PyMethodDef, PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyCMethod_New", ret = PyObject, args = {PyMethodDef, PyObject, PyObject, PyTypeObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyContextVar_Get", ret = Int, args = {PyObject, PyObject, PyObjectPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyDescr_NewClassMethod", ret = PyObject, args = {PyTypeObject, PyMethodDef}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyDescr_NewGetSet", ret = PyObject, args = {PyTypeObject, PyGetSetDef}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyDescrObject_GetName", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyDescrObject_GetType", ret = PyTypeObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_BadArgument", ret = Int, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_BadInternalCall", ret = Void, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_CheckSignals", ret = Int, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_Clear", ret = Void, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_ExceptionMatches", ret = Int, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_Fetch", ret = Void, args = {PyObjectPtr, PyObjectPtr, PyObjectPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_Format", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, VARARGS}, forwardsTo = "PyErr_FormatV", call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_FormatV", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_GetExcInfo", ret = Void, args = {PyObjectPtr, PyObjectPtr, PyObjectPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_NoMemory", ret = PyObject, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_NormalizeException", ret = Void, args = {PyObjectPtr, PyObjectPtr, PyObjectPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_Print", ret = Void, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_SetFromErrno", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_SetFromErrnoWithFilename", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_SetFromErrnoWithFilenameObject", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_SetFromErrnoWithFilenameObjects", ret = PyObject, args = {PyObject, PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_SetNone", ret = Void, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_SetObject", ret = Void, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_SetString", ret = Void, args = {PyObject, ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyErr_WriteUnraisable", ret = Void, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyEval_CallObjectWithKeywords", ret = PyObject, args = {PyObject, PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyEval_EvalCode", ret = PyObject, args = {PyObject, PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyEval_EvalCodeEx", ret = PyObject, args = {PyObject, PyObject, PyObject, PyObjectConstPtr, Int, PyObjectConstPtr, Int, PyObjectConstPtr, Int, PyObject,
                    PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyEval_InitThreads", ret = Void, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyEval_MergeCompilerFlags", ret = Int, args = {PY_COMPILER_FLAGS}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyEval_ThreadsInitialized", ret = Int, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyFile_WriteString", ret = Int, args = {ConstCharPtrAsTruffleString, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyGen_New", ret = PyObject, args = {PyFrameObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyGen_NewWithQualName", ret = PyObject, args = {PyFrameObject, PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyGILState_GetThisThreadState", ret = PyThreadState, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyImport_AddModule", ret = PyObject, args = {ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyImport_AddModuleObject", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyImport_ImportModuleLevel", ret = PyObject, args = {ConstCharPtrAsTruffleString, PyObject, PyObject, PyObject, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyInstanceMethod_Function", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyInterpreterState_GetID", ret = INT64_T, args = {PyInterpreterState}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyInterpreterState_GetIDFromThreadState", ret = INT64_T, args = {PyThreadState}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyInterpreterState_Main", ret = PyInterpreterState, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyIter_Check", ret = Int, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyLong_FromString", ret = PyObject, args = {ConstCharPtrAsTruffleString, CHAR_PTR_LIST, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyMapping_GetItemString", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyMemoryView_FromBuffer", ret = PyObject, args = {PY_BUFFER}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyMemoryView_FromMemory", ret = PyObject, args = {CHAR_PTR, Py_ssize_t, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyMethod_Function", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyMethod_Self", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyMethodDescrObject_GetMethod", ret = PyMethodDef, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyModule_AddFunctions", ret = Int, args = {PyObject, PyMethodDef}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyModule_Create2", ret = PyObject, args = {PYMODULEDEF_PTR, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyModule_GetDef", ret = PyModuleDef, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyModule_GetDict", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyModule_GetName", ret = ConstCharPtrAsTruffleString, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyModule_GetState", ret = Pointer, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyModuleDef_Init", ret = PyObject, args = {PYMODULEDEF_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_VectorcallMethod", ret = PyObject, args = {PyObject, PyObjectConstPtr, SIZE_T, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Add", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_And", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_AsSsize_t", ret = Py_ssize_t, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_FloorDivide", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceAdd", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceAnd", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceFloorDivide", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceLshift", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceMatrixMultiply", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceMultiply", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceOr", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceRemainder", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceRshift", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceSubtract", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceTrueDivide", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_InPlaceXor", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Invert", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Lshift", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_MatrixMultiply", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Multiply", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Negative", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Or", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Positive", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Remainder", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Rshift", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Subtract", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_TrueDivide", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyNumber_Xor", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_CallFunctionObjArgs", ret = PyObject, args = {PyObject, VARARGS}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_CallMethodObjArgs", ret = PyObject, args = {PyObject, PyObject, VARARGS}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_CheckBuffer", ret = Int, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_GenericGetAttr", ret = PyObjectTransfer, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_GenericSetAttr", ret = Int, args = {PyObject, PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_GenericSetDict", ret = Int, args = {PyObject, PyObject, Pointer}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_GetAttr", ret = PyObject, args = {PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_GetAttrString", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_GetBuffer", ret = Int, args = {PyObject, PY_BUFFER, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_Not", ret = Int, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_Print", ret = Int, args = {PyObject, FILE_PTR, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_RichCompareBool", ret = Int, args = {PyObject, PyObject, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_SelfIter", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_SetAttr", ret = Int, args = {PyObject, PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_SetAttrString", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyObject_VectorcallDict", ret = PyObject, args = {PyObject, PyObjectConstPtr, SIZE_T, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyOS_double_to_string", ret = CHAR_PTR, args = {Double, CHAR, Int, Int, INT_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyOS_snprintf", ret = Int, args = {CHAR_PTR, SIZE_T, ConstCharPtrAsTruffleString, VARARGS}, forwardsTo = "PyOS_vsnprintf", call = PolyglotImpl)
    @CApiBuiltin(name = "PyOS_string_to_double", ret = Double, args = {ConstCharPtrAsTruffleString, CHAR_PTR_LIST, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyOS_strtol", ret = Long, args = {ConstCharPtrAsTruffleString, CHAR_PTR_LIST, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyOS_strtoul", ret = UNSIGNED_LONG, args = {ConstCharPtrAsTruffleString, CHAR_PTR_LIST, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyOS_vsnprintf", ret = Int, args = {CHAR_PTR, SIZE_T, ConstCharPtrAsTruffleString, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "PySequence_Fast", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PySlice_AdjustIndices", ret = Py_ssize_t, args = {Py_ssize_t, PY_SSIZE_T_PTR, PY_SSIZE_T_PTR, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PySlice_Start", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PySlice_Step", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PySlice_Stop", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PySlice_Unpack", ret = Int, args = {PyObject, PY_SSIZE_T_PTR, PY_SSIZE_T_PTR, PY_SSIZE_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyState_AddModule", ret = Int, args = {PyObject, PYMODULEDEF_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyState_FindModule", ret = PyObjectBorrowed, args = {PYMODULEDEF_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyState_RemoveModule", ret = Int, args = {PYMODULEDEF_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyStructSequence_GetItem", ret = PyObject, args = {PyObject, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyStructSequence_InitType", ret = Void, args = {PyTypeObject, PY_STRUCT_SEQUENCE_DESC}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyStructSequence_InitType2", ret = Int, args = {PyTypeObject, PY_STRUCT_SEQUENCE_DESC}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyStructSequence_NewType", ret = PyTypeObject, args = {PY_STRUCT_SEQUENCE_DESC}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyStructSequence_SetItem", ret = Void, args = {PyObject, Py_ssize_t, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThread_free_lock", ret = Void, args = {PY_THREAD_TYPE_LOCK}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThread_tss_alloc", ret = PY_TSS_T_PTR, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThread_tss_create", ret = Int, args = {PY_TSS_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThread_tss_delete", ret = Void, args = {PY_TSS_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThread_tss_free", ret = Void, args = {PY_TSS_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThread_tss_get", ret = Pointer, args = {PY_TSS_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThread_tss_is_created", ret = Int, args = {PY_TSS_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThread_tss_set", ret = Int, args = {PY_TSS_T_PTR, Pointer}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThreadState_Clear", ret = Void, args = {PyThreadState}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyThreadState_DeleteCurrent", ret = Void, args = {}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyTuple_Pack", ret = PyObject, args = {Py_ssize_t, VARARGS}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_FromModuleAndSpec", ret = PyObject, args = {PyObject, PY_TYPE_SPEC, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_FromSpec", ret = PyObject, args = {PY_TYPE_SPEC}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_FromSpecWithBases", ret = PyObject, args = {PY_TYPE_SPEC, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_GenericAlloc", ret = PyObject, args = {PyTypeObject, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_GenericNew", ret = PyObject, args = {PyTypeObject, PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_GetModule", ret = PyObject, args = {PyTypeObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_GetModuleState", ret = Pointer, args = {PyTypeObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_GetSlot", ret = Pointer, args = {PyTypeObject, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_Modified", ret = Void, args = {PyTypeObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyType_Ready", ret = Int, args = {PyTypeObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_Append", ret = Void, args = {PyObjectPtr, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AppendAndDel", ret = Void, args = {PyObjectPtr, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsASCIIString", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsLatin1String", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsUCS4", ret = PY_UCS4_PTR, args = {PyObject, PY_UCS4_PTR, Py_ssize_t, Int}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsUCS4Copy", ret = PY_UCS4_PTR, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsUnicode", ret = PY_UNICODE_PTR, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsUnicodeAndSize", ret = PY_UNICODE_PTR, args = {PyObject, PY_SSIZE_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsUTF8", ret = ConstCharPtrAsTruffleString, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsUTF8AndSize", ret = ConstCharPtrAsTruffleString, args = {PyObject, PY_SSIZE_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsUTF8String", ret = PyObject, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_AsWideChar", ret = Py_ssize_t, args = {PyObject, WCHAR_T_PTR, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_Decode", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_DecodeASCII", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_DecodeFSDefaultAndSize", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_DecodeLatin1", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_DecodeUTF32", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString, INT_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_DecodeUTF8", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_DecodeUTF8Stateful", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString, PY_SSIZE_T_PTR}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_FromFormat", ret = PyObject, args = {ConstCharPtrAsTruffleString, VARARGS}, forwardsTo = "PyUnicode_FromFormatV", call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_FromFormatV", ret = PyObject, args = {ConstCharPtrAsTruffleString, VA_LIST}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_FromKindAndData", ret = PyObject, args = {Int, CONST_VOID_PTR, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_FromStringAndSize", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_FromUnicode", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_FromWideChar", ret = PyObject, args = {CONST_WCHAR_PTR, Py_ssize_t}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_FSConverter", ret = Int, args = {PyObject, Pointer}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_GetLength", ret = Py_ssize_t, args = {PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_InternFromString", ret = PyObject, args = {ConstCharPtrAsTruffleString}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_InternInPlace", ret = Void, args = {PyObjectPtr}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyUnicode_New", ret = PyObject, args = {Py_ssize_t, PY_UCS4}, call = PolyglotImpl)
    @CApiBuiltin(name = "PyVectorcall_Call", ret = PyObject, args = {PyObject, PyObject, PyObject}, call = PolyglotImpl)
    @CApiBuiltin(name = "_PyObject_CallMethodIdObjArgs", ret = PyObject, args = {PyObject, _PY_IDENTIFIER_PTR, VARARGS}, call = PolyglotImpl)

    /*
     * Functions that are not implemented at the moment:
     */
    @CApiBuiltin(name = "_PyArg_ParseStackAndKeywords_SizeT", ret = Int, args = {PyObjectConstPtr, Py_ssize_t, PyObject, _PYARG_PARSER_PTR, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_abspath", ret = Int, args = {CONST_WCHAR_PTR, WCHAR_T_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_add_one_to_index_C", ret = Void, args = {Int, PY_SSIZE_T_PTR, CONST_PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_add_one_to_index_F", ret = Void, args = {Int, PY_SSIZE_T_PTR, CONST_PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_BreakPoint", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_CheckFunctionResult", ret = PyObject, args = {PyThreadState, PyObject, PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_CheckRecursiveCall", ret = Int, args = {PyThreadState, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_CoerceLegacyLocale", ret = Int, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_convert_optional_to_ssize_t", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_DecodeLocaleEx", ret = Int, args = {ConstCharPtrAsTruffleString, WCHAR_T_PTR_LIST, SIZE_T_PTR, CONST_CHAR_PTR_LIST, Int, _PY_ERROR_HANDLER}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_device_encoding", ret = PyObject, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_dg_dtoa", ret = CHAR_PTR, args = {Double, Int, Int, INT_LIST, INT_LIST, CHAR_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_dg_freedtoa", ret = Void, args = {CHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_dg_infinity", ret = Double, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_dg_stdnan", ret = Double, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_dg_strtod", ret = Double, args = {ConstCharPtrAsTruffleString, CHAR_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_DisplaySourceLine", ret = Int, args = {PyObject, PyObject, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_dup", ret = Int, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_EncodeLocaleEx", ret = Int, args = {CONST_WCHAR_PTR, CHAR_PTR_LIST, SIZE_T_PTR, CONST_CHAR_PTR_LIST, Int, _PY_ERROR_HANDLER}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_EncodeLocaleRaw", ret = CHAR_PTR, args = {CONST_WCHAR_PTR, SIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_FatalErrorFormat", ret = Void, args = {ConstCharPtr, ConstCharPtr, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_FdIsInteractive", ret = Int, args = {FILE_PTR, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_fopen_obj", ret = FILE_PTR, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_FreeCharPArray", ret = Void, args = {CHAR_CONST_ARRAY}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_fstat_noraise", ret = Int, args = {Int, STAT_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_fstat", ret = Int, args = {Int, STAT_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_get_blocking", ret = Int, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_get_inheritable", ret = Int, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_GetAllocatedBlocks", ret = Py_ssize_t, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_GetConfig", ret = CONST_PYCONFIG_PTR, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_InitializeMain", ret = PYSTATUS, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_isabs", ret = Int, args = {CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_IsCoreInitialized", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_IsFinalizing", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_LegacyLocaleDetected", ret = Int, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_Mangle", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_NewInterpreter", ret = PyThreadState, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_open_noraise", ret = Int, args = {ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_open", ret = Int, args = {ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_read", ret = Py_ssize_t, args = {Int, Pointer, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_RestoreSignals", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_set_blocking", ret = Int, args = {Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_set_inheritable_async_safe", ret = Int, args = {Int, Int, INT_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_set_inheritable", ret = Int, args = {Int, Int, INT_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_SetLocaleFromEnv", ret = CHAR_PTR, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_SetProgramFullPath", ret = Void, args = {CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_SourceAsString", ret = ConstCharPtrAsTruffleString, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, PY_COMPILER_FLAGS,
                    PyObjectPtr}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_stat", ret = Int, args = {PyObject, STAT_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_wfopen", ret = FILE_PTR, args = {CONST_WCHAR_PTR, CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_wgetcwd", ret = WCHAR_T_PTR, args = {WCHAR_T_PTR, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_wreadlink", ret = Int, args = {CONST_WCHAR_PTR, WCHAR_T_PTR, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_wrealpath", ret = WCHAR_T_PTR, args = {CONST_WCHAR_PTR, WCHAR_T_PTR, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_write_noraise", ret = Py_ssize_t, args = {Int, CONST_VOID_PTR, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "_Py_write", ret = Py_ssize_t, args = {Int, CONST_VOID_PTR, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyArg_Fini", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyArg_NoKwnames", ret = Int, args = {ConstCharPtr, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyArg_ParseStack", ret = Int, args = {PyObjectConstPtr, Py_ssize_t, ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "_PyArg_ParseStackAndKeywords", ret = Int, args = {PyObjectConstPtr, Py_ssize_t, PyObject, _PYARG_PARSER_PTR, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "_PyAsyncGenValueWrapperNew", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyBytes_DecodeEscape", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString, CONST_CHAR_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "_PyBytes_FormatEx", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyBytes_FromHex", ret = PyObject, args = {PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCode_CheckLineNumber", ret = Int, args = {Int, PyCodeAddressRange}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCode_ConstantKey", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCode_GetExtra", ret = Int, args = {PyObject, Py_ssize_t, VOID_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCode_InitAddressRange", ret = Int, args = {PyCodeObject, PyCodeAddressRange}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCode_SetExtra", ret = Int, args = {PyObject, Py_ssize_t, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCodec_DecodeText", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCodec_EncodeText", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCodec_Forget", ret = Int, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCodec_Lookup", ret = PyObject, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCodec_LookupTextEncoding", ret = PyObject, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCodecInfo_GetIncrementalDecoder", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCodecInfo_GetIncrementalEncoder", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyComplex_FormatAdvancedWriter", ret = Int, args = {_PYUNICODEWRITER_PTR, PyObject, PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyContext_NewHamtForTests", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCoro_GetAwaitableIter", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCrossInterpreterData_Lookup", ret = CROSSINTERPDATAFUNC, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCrossInterpreterData_NewObject", ret = PyObject, args = {_PYCROSSINTERPRETERDATA_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCrossInterpreterData_RegisterClass", ret = Int, args = {PyTypeObject, CROSSINTERPDATAFUNC}, call = NotImplemented)
    @CApiBuiltin(name = "_PyCrossInterpreterData_Release", ret = Void, args = {_PYCROSSINTERPRETERDATA_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDebugAllocatorStats", ret = Void, args = {FILE_PTR, ConstCharPtrAsTruffleString, Int, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_Contains_KnownHash", ret = Int, args = {PyObject, PyObject, Py_hash_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_DebugMallocStats", ret = Void, args = {FILE_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_DelItem_KnownHash", ret = Int, args = {PyObject, PyObject, Py_hash_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_DelItemId", ret = Int, args = {PyObject, _PY_IDENTIFIER_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_DelItemIf", ret = Int, args = {PyObject, PyObject, func_objint}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_FromKeys", ret = PyObject, args = {PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_GetItemHint", ret = Py_ssize_t, args = {PYDICTOBJECT_PTR, PyObject, Py_ssize_t, PyObjectPtr}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_HasOnlyStringKeys", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_KeysSize", ret = Py_ssize_t, args = {PYDICTKEYSOBJECT_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_LoadGlobal", ret = PyObject, args = {PYDICTOBJECT_PTR, PYDICTOBJECT_PTR, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_MaybeUntrack", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_MergeEx", ret = Int, args = {PyObject, PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_NewKeysForClass", ret = PYDICTKEYSOBJECT_PTR, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_Pop_KnownHash", ret = PyObject, args = {PyObject, PyObject, Py_hash_t, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDict_SizeOf", ret = Py_ssize_t, args = {PYDICTOBJECT_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDictView_Intersect", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyDictView_New", ret = PyObject, args = {PyObject, PyTypeObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyErr_ChainExceptions", ret = Void, args = {PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyErr_CheckSignals", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyErr_FormatFromCause", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "_PyErr_GetExcInfo", ret = Void, args = {PyThreadState, PyObjectPtr, PyObjectPtr, PyObjectPtr}, call = NotImplemented)
    @CApiBuiltin(name = "_PyErr_GetTopmostException", ret = _PYERR_STACKITEM_PTR, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "_PyErr_ProgramDecodedTextObject", ret = PyObject, args = {PyObject, Int, ConstCharPtr}, call = NotImplemented)
    @CApiBuiltin(name = "_PyErr_SetKeyError", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyErr_TrySetFromCause", ret = PyObject, args = {ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "_PyErr_WarnUnawaitedCoroutine", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_CallTracing", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_EvalFrameDefault", ret = PyObject, args = {PyThreadState, PyFrameObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_GetAsyncGenFinalizer", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_GetAsyncGenFirstiter", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_GetBuiltinId", ret = PyObject, args = {PY_IDENTIFIER}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_GetCoroutineOriginTrackingDepth", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_GetSwitchInterval", ret = UNSIGNED_LONG, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_RequestCodeExtraIndex", ret = Py_ssize_t, args = {FREEFUNC}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_SetAsyncGenFinalizer", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_SetAsyncGenFirstiter", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_SetCoroutineOriginTrackingDepth", ret = Void, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_SetProfile", ret = Int, args = {PyThreadState, PY_TRACEFUNC, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_SetSwitchInterval", ret = Void, args = {UNSIGNED_LONG}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_SetTrace", ret = Int, args = {PyThreadState, PY_TRACEFUNC, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyEval_SliceIndexNotNone", ret = Int, args = {PyObject, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyFloat_DebugMallocStats", ret = Void, args = {FILE_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyFloat_FormatAdvancedWriter", ret = Int, args = {_PYUNICODEWRITER_PTR, PyObject, PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyFrame_DebugMallocStats", ret = Void, args = {FILE_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyFrame_New_NoTrack", ret = PyFrameObject, args = {PyThreadState, PyFrameConstructor, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyFunction_Vectorcall", ret = PyObject, args = {PyObject, PyObjectConstPtr, SIZE_T, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyGILState_GetInterpreterStateUnsafe", ret = PyInterpreterState, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_AcquireLock", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_FindExtensionObject", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_FixupBuiltin", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_FixupExtensionObject", ret = Int, args = {PyObject, PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_GetModuleAttr", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_GetModuleAttrString", ret = PyObject, args = {ConstCharPtr, ConstCharPtr}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_GetModuleId", ret = PyObject, args = {_PY_IDENTIFIER_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_IsInitialized", ret = Int, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_ReInitLock", ret = PYSTATUS, args = {}, call = NotImplemented, comment = "depends on HAVE_FORK")
    @CApiBuiltin(name = "_PyImport_ReleaseLock", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyImport_SetModuleString", ret = Int, args = {ConstCharPtrAsTruffleString, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyInterpreterState_GetConfig", ret = CONST_PYCONFIG_PTR, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "_PyInterpreterState_GetConfigCopy", ret = Int, args = {PYCONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyInterpreterState_GetEvalFrameFunc", ret = _PyFrameEvalFunction, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "_PyInterpreterState_GetMainModule", ret = PyObject, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "_PyInterpreterState_RequireIDRef", ret = Void, args = {PyInterpreterState, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyInterpreterState_RequiresIDRef", ret = Int, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "_PyInterpreterState_SetConfig", ret = Int, args = {CONST_PYCONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyInterpreterState_SetEvalFrameFunc", ret = Void, args = {PyInterpreterState, _PyFrameEvalFunction}, call = NotImplemented)
    @CApiBuiltin(name = "_PyList_DebugMallocStats", ret = Void, args = {FILE_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_AsTime_t", ret = TIME_T, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_Copy", ret = PyObject, args = {PyLongObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_DivmodNear", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_FileDescriptor_Converter", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_Format", ret = PyObject, args = {PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_FormatAdvancedWriter", ret = Int, args = {_PYUNICODEWRITER_PTR, PyObject, PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_FormatBytesWriter", ret = CHAR_PTR, args = {_PYBYTESWRITER_PTR, CHAR_PTR, PyObject, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_FormatWriter", ret = Int, args = {_PYUNICODEWRITER_PTR, PyObject, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_Frexp", ret = Double, args = {PyLongObject, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_FromByteArray", ret = PyObject, args = {CONST_UNSIGNED_CHAR_PTR, SIZE_T, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_FromBytes", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_GCD", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_Lshift", ret = PyObject, args = {PyObject, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_New", ret = PyLongObject, args = {Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_NumBits", ret = SIZE_T, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_Rshift", ret = PyObject, args = {PyObject, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_Size_t_Converter", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_UnsignedInt_Converter", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_UnsignedLong_Converter", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_UnsignedLongLong_Converter", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_PyLong_UnsignedShort_Converter", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_PyMem_GetCurrentAllocatorName", ret = ConstCharPtrAsTruffleString, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyMem_RawStrdup", ret = CHAR_PTR, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyMem_RawWcsdup", ret = WCHAR_T_PTR, args = {CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyMem_Strdup", ret = CHAR_PTR, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyModule_Clear", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyModule_ClearDict", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyModuleSpec_IsInitializing", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_AssertFailed", ret = VoidNoReturn, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, Int,
                    ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_Call_Prepend", ret = PyObject, args = {PyThreadState, PyObject, PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_CallMethodId_SizeT", ret = PyObject, args = {PyObject, PY_IDENTIFIER, ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_CallMethodId", ret = PyObject, args = {PyObject, PY_IDENTIFIER, ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_CheckConsistency", ret = Int, args = {PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_CheckCrossInterpreterData", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_DebugMallocStats", ret = Int, args = {FILE_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_DebugTypeStats", ret = Void, args = {FILE_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_FunctionStr", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_GC_Resize", ret = PyVarObject, args = {PyVarObject, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_GenericGetAttrWithDict", ret = PyObject, args = {PyObject, PyObject, PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_GenericSetAttrWithDict", ret = Int, args = {PyObject, PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_GetCrossInterpreterData", ret = Int, args = {PyObject, _PYCROSSINTERPRETERDATA_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_HasLen", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_IsAbstract", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_IsFreed", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_LookupSpecial", ret = PyObject, args = {PyObject, PY_IDENTIFIER}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_RealIsInstance", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObject_RealIsSubclass", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyObjectDict_SetItem", ret = Int, args = {PyTypeObject, PyObjectPtr, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyOS_IsMainThread", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyOS_URandom", ret = Int, args = {Pointer, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyOS_URandomNonblock", ret = Int, args = {Pointer, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyRun_AnyFileObject", ret = Int, args = {FILE_PTR, PyObject, Int, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "_PyRun_InteractiveLoopObject", ret = Int, args = {FILE_PTR, PyObject, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "_PyRun_SimpleFileObject", ret = Int, args = {FILE_PTR, PyObject, Int, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "_PySequence_BytesToCharpArray", ret = CHAR_CONST_PTR, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PySequence_IterSearch", ret = Py_ssize_t, args = {PyObject, PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PySet_Update", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PySignal_AfterFork", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PySlice_FromIndices", ret = PyObject, args = {Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PySlice_GetLongIndices", ret = Int, args = {PySliceObject, PyObject, PyObjectPtr, PyObjectPtr, PyObjectPtr}, call = NotImplemented)
    @CApiBuiltin(name = "_PyStack_AsDict", ret = PyObject, args = {PyObjectConstPtr, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyState_AddModule", ret = Int, args = {PyThreadState, PyObject, PYMODULEDEF_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PySys_GetObjectId", ret = PyObject, args = {PY_IDENTIFIER}, call = NotImplemented)
    @CApiBuiltin(name = "_PySys_GetSizeOf", ret = SIZE_T, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PySys_SetObjectId", ret = Int, args = {PY_IDENTIFIER, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyThread_at_fork_reinit", ret = Int, args = {PY_THREAD_TYPE_LOCK_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyThread_CurrentExceptions", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyThread_CurrentFrames", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyThreadState_GetDict", ret = PyObject, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "_PyThreadState_Prealloc", ret = PyThreadState, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_AsMicroseconds", ret = _PYTIME_T, args = {_PYTIME_T, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_AsMilliseconds", ret = _PYTIME_T, args = {_PYTIME_T, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_AsNanosecondsObject", ret = PyObject, args = {_PYTIME_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_AsSecondsDouble", ret = Double, args = {_PYTIME_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_AsTimespec", ret = Int, args = {_PYTIME_T, TIMESPEC_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_AsTimeval_noraise", ret = Int, args = {_PYTIME_T, TIMEVAL_PTR, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_AsTimeval", ret = Int, args = {_PYTIME_T, TIMEVAL_PTR, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_AsTimevalTime_t", ret = Int, args = {_PYTIME_T, TIME_T_PTR, INT_LIST, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_FromMillisecondsObject", ret = Int, args = {_PYTIME_T_PTR, PyObject, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_FromNanoseconds", ret = _PYTIME_T, args = {_PYTIME_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_FromNanosecondsObject", ret = Int, args = {_PYTIME_T_PTR, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_FromSeconds", ret = _PYTIME_T, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_FromSecondsObject", ret = Int, args = {_PYTIME_T_PTR, PyObject, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_FromTimespec", ret = Int, args = {_PYTIME_T_PTR, TIMESPEC_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_FromTimeval", ret = Int, args = {_PYTIME_T_PTR, TIMEVAL_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_GetMonotonicClock", ret = _PYTIME_T, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_GetMonotonicClockWithInfo", ret = Int, args = {_PYTIME_T_PTR, _PY_CLOCK_INFO_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_GetPerfCounter", ret = _PYTIME_T, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_GetPerfCounterWithInfo", ret = Int, args = {_PYTIME_T_PTR, _PY_CLOCK_INFO_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_GetSystemClock", ret = _PYTIME_T, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_GetSystemClockWithInfo", ret = Int, args = {_PYTIME_T_PTR, _PY_CLOCK_INFO_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_gmtime", ret = Int, args = {TIME_T, TM_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_localtime", ret = Int, args = {TIME_T, TM_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_MulDiv", ret = _PYTIME_T, args = {_PYTIME_T, _PYTIME_T, _PYTIME_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_ObjectToTime_t", ret = Int, args = {PyObject, TIME_T_PTR, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_ObjectToTimespec", ret = Int, args = {PyObject, TIME_T_PTR, LONG_PTR, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTime_ObjectToTimeval", ret = Int, args = {PyObject, TIME_T_PTR, LONG_PTR, _PYTIME_ROUND_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTraceMalloc_GetTraceback", ret = PyObject, args = {UNSIGNED_INT, UINTPTR_T}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTrash_begin", ret = Int, args = {TS_PTR, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTrash_cond", ret = Int, args = {PyObject, destructor}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTrash_deposit_object", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTrash_destroy_chain", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTrash_end", ret = Void, args = {TS_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTrash_thread_deposit_object", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTrash_thread_destroy_chain", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTuple_DebugMallocStats", ret = Void, args = {FILE_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTuple_MaybeUntrack", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyTuple_Resize", ret = Int, args = {PyObjectPtr, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyType_CalculateMetaclass", ret = PyTypeObject, args = {PyTypeObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyType_GetDocFromInternalDoc", ret = PyObject, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyType_GetTextSignatureFromInternalDoc", ret = PyObject, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyType_LookupId", ret = PyObject, args = {PyTypeObject, PY_IDENTIFIER}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_AsUnicode", ret = CONST_PY_UNICODE, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_CheckConsistency", ret = Int, args = {PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_Copy", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_DecodeRawUnicodeEscapeStateful", ret = PyObject, args = {ConstCharPtr, Py_ssize_t, ConstCharPtr, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_DecodeUnicodeEscapeInternal", ret = PyObject, args = {ConstCharPtr, Py_ssize_t, ConstCharPtr, PY_SSIZE_T_PTR, CONST_CHAR_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_DecodeUnicodeEscapeStateful", ret = PyObject, args = {ConstCharPtr, Py_ssize_t, ConstCharPtr, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_EncodeCharmap", ret = PyObject, args = {PyObject, PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_EncodeUTF16", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_EncodeUTF32", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_EncodeUTF7", ret = PyObject, args = {PyObject, Int, Int, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_EQ", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_FastCopyCharacters", ret = Void, args = {PyObject, Py_ssize_t, PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_FastFill", ret = Void, args = {PyObject, Py_ssize_t, Py_ssize_t, PY_UCS4}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_FindMaxChar", ret = PY_UCS4, args = {PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_FormatAdvancedWriter", ret = Int, args = {_PYUNICODEWRITER_PTR, PyObject, PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_FormatLong", ret = PyObject, args = {PyObject, Int, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_FromASCII", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_InsertThousandsGrouping", ret = Py_ssize_t, args = {_PYUNICODEWRITER_PTR, Py_ssize_t, PyObject, Py_ssize_t, Py_ssize_t, Py_ssize_t, ConstCharPtrAsTruffleString,
                    PyObject, PY_UCS4_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_JoinArray", ret = PyObject, args = {PyObject, PyObjectConstPtr, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_ScanIdentifier", ret = Py_ssize_t, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_TransformDecimalAndSpaceToASCII", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_WideCharString_Converter", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_WideCharString_Opt_Converter", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicode_XStrip", ret = PyObject, args = {PyObject, Int, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeTranslateError_Create", ret = PyObject, args = {PyObject, Py_ssize_t, Py_ssize_t, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_Dealloc", ret = Void, args = {_PYUNICODEWRITER_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_Finish", ret = PyObject, args = {_PYUNICODEWRITER_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_Init", ret = Void, args = {_PYUNICODEWRITER_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_PrepareInternal", ret = Int, args = {_PYUNICODEWRITER_PTR, Py_ssize_t, PY_UCS4}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_PrepareKindInternal", ret = Int, args = {_PYUNICODEWRITER_PTR, PYUNICODE_KIND}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_WriteASCIIString", ret = Int, args = {_PYUNICODEWRITER_PTR, ConstCharPtrAsTruffleString, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_WriteChar", ret = Int, args = {_PYUNICODEWRITER_PTR, PY_UCS4}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_WriteLatin1String", ret = Int, args = {_PYUNICODEWRITER_PTR, ConstCharPtrAsTruffleString, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_WriteStr", ret = Int, args = {_PYUNICODEWRITER_PTR, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "_PyUnicodeWriter_WriteSubstring", ret = Int, args = {_PYUNICODEWRITER_PTR, PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "_PyWarnings_Init", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "_PyWeakref_ClearRef", ret = Void, args = {PYWEAKREFERENCE_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "_PyWeakref_GetWeakrefCount", ret = Py_ssize_t, args = {PYWEAKREFERENCE_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "Py_AddPendingCall", ret = Int, args = {func_intvoidptr, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "Py_BytesMain", ret = Int, args = {Int, CHAR_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "Py_CompileString", ret = PyObject, args = {ConstCharPtr, ConstCharPtr, Int}, call = NotImplemented)
    @CApiBuiltin(name = "Py_CompileStringExFlags", ret = PyObject, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, Int, PY_COMPILER_FLAGS, Int}, call = NotImplemented)
    @CApiBuiltin(name = "Py_CompileStringObject", ret = PyObject, args = {ConstCharPtrAsTruffleString, PyObject, Int, PY_COMPILER_FLAGS, Int}, call = NotImplemented)
    @CApiBuiltin(name = "Py_DecodeLocale", ret = WCHAR_T_PTR, args = {ConstCharPtrAsTruffleString, SIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "Py_EncodeLocale", ret = CHAR_PTR, args = {CONST_WCHAR_PTR, SIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "Py_EndInterpreter", ret = Void, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "Py_Exit", ret = VoidNoReturn, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "Py_ExitStatusException", ret = VoidNoReturn, args = {PYSTATUS}, call = NotImplemented)
    @CApiBuiltin(name = "Py_FatalError", ret = VoidNoReturn, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "Py_FdIsInteractive", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "Py_Finalize", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_FinalizeEx", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_FrozenMain", ret = Int, args = {Int, CHAR_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetArgcArgv", ret = Void, args = {INT_LIST, WCHAR_T_PTR_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetCopyright", ret = ConstCharPtrAsTruffleString, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetExecPrefix", ret = WCHAR_T_PTR, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetPath", ret = WCHAR_T_PTR, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetPlatform", ret = ConstCharPtrAsTruffleString, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetPrefix", ret = WCHAR_T_PTR, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetProgramFullPath", ret = WCHAR_T_PTR, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetProgramName", ret = WCHAR_T_PTR, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetPythonHome", ret = WCHAR_T_PTR, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_GetRecursionLimit", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_Initialize", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_InitializeEx", ret = Void, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "Py_InitializeFromConfig", ret = PYSTATUS, args = {CONST_PYCONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "Py_Main", ret = Int, args = {Int, WCHAR_T_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "Py_MakePendingCalls", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_NewInterpreter", ret = PyThreadState, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_PreInitialize", ret = PYSTATUS, args = {CONST_PYPRECONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "Py_PreInitializeFromArgs", ret = PYSTATUS, args = {CONST_PYPRECONFIG_PTR, Py_ssize_t, WCHAR_T_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "Py_PreInitializeFromBytesArgs", ret = PYSTATUS, args = {CONST_PYPRECONFIG_PTR, Py_ssize_t, CHAR_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "Py_ReprEnter", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "Py_ReprLeave", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "Py_RunMain", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "Py_SetPath", ret = Void, args = {CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "Py_SetProgramName", ret = Void, args = {CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "Py_SetPythonHome", ret = Void, args = {CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "Py_SetRecursionLimit", ret = Void, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "Py_SetStandardStreamEncoding", ret = Int, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "Py_UniversalNewlineFgets", ret = CHAR_PTR, args = {CHAR_PTR, Int, FILE_PTR, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyAIter_Check", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyArg_ValidateKeywordArguments", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyAsyncGen_New", ret = PyObject, args = {PyFrameObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyBuffer_FillContiguousStrides", ret = Void, args = {Int, PY_SSIZE_T_PTR, PY_SSIZE_T_PTR, Int, CHAR}, call = NotImplemented)
    @CApiBuiltin(name = "PyBuffer_FromContiguous", ret = Int, args = {PY_BUFFER, Pointer, Py_ssize_t, CHAR}, call = NotImplemented)
    @CApiBuiltin(name = "PyBuffer_GetPointer", ret = Pointer, args = {PY_BUFFER, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyBuffer_SizeFromFormat", ret = Py_ssize_t, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyBuffer_ToContiguous", ret = Int, args = {Pointer, PY_BUFFER, Py_ssize_t, CHAR}, call = NotImplemented)
    @CApiBuiltin(name = "PyByteArray_AsString", ret = CHAR_PTR, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyByteArray_Concat", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyByteArray_FromObject", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyByteArray_Size", ret = Py_ssize_t, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyBytes_DecodeEscape", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString, Py_ssize_t,
                    ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyBytes_Repr", ret = PyObject, args = {PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyCell_Get", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCell_New", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCell_Set", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCFunction_Call", ret = PyObject, args = {PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCode_Addr2Line", ret = Int, args = {PyCodeObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyCode_Optimize", ret = PyObject, args = {PyObject, PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_BackslashReplaceErrors", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_Decode", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_Decoder", ret = PyObject, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_Encode", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_Encoder", ret = PyObject, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_IgnoreErrors", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_IncrementalDecoder", ret = PyObject, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_IncrementalEncoder", ret = PyObject, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_KnownEncoding", ret = Int, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_LookupError", ret = PyObject, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_NameReplaceErrors", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_Register", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_RegisterError", ret = Int, args = {ConstCharPtrAsTruffleString, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_ReplaceErrors", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_StreamReader", ret = PyObject, args = {ConstCharPtrAsTruffleString, PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_StreamWriter", ret = PyObject, args = {ConstCharPtrAsTruffleString, PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_StrictErrors", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_Unregister", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCodec_XMLCharRefReplaceErrors", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCompile_OpcodeStackEffect", ret = Int, args = {Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyCompile_OpcodeStackEffectWithJump", ret = Int, args = {Int, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyConfig_Clear", ret = Void, args = {PYCONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyConfig_InitIsolatedConfig", ret = Void, args = {PYCONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyConfig_InitPythonConfig", ret = Void, args = {PYCONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyConfig_Read", ret = PYSTATUS, args = {PYCONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyConfig_SetArgv", ret = PYSTATUS, args = {PYCONFIG_PTR, Py_ssize_t, WCHAR_T_CONST_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyConfig_SetBytesArgv", ret = PYSTATUS, args = {PYCONFIG_PTR, Py_ssize_t, CHAR_CONST_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyConfig_SetBytesString", ret = PYSTATUS, args = {PYCONFIG_PTR, WCHAR_T_PTR_LIST, ConstCharPtr}, call = NotImplemented)
    @CApiBuiltin(name = "PyConfig_SetString", ret = PYSTATUS, args = {PYCONFIG_PTR, WCHAR_T_PTR_LIST, CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyConfig_SetWideStringList", ret = PYSTATUS, args = {PYCONFIG_PTR, PYWIDESTRINGLIST_PTR, Py_ssize_t, WCHAR_T_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "PyContext_Copy", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyContext_CopyCurrent", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyContext_Enter", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyContext_Exit", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyContext_New", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyContextVar_Reset", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyCoro_New", ret = PyObject, args = {PyFrameObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyDescr_NewMember", ret = PyObject, args = {PyTypeObject, PyMemberDef}, call = NotImplemented)
    @CApiBuiltin(name = "PyDescr_NewMethod", ret = PyObject, args = {PyTypeObject, PyMethodDef}, call = NotImplemented)
    @CApiBuiltin(name = "PyDescr_NewWrapper", ret = PyObject, args = {PyTypeObject, WRAPPERBASE, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "PyDict_Items", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyDict_MergeFromSeq2", ret = Int, args = {PyObject, PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_ProgramText", ret = PyObject, args = {ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_ProgramTextObject", ret = PyObject, args = {PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_RangedSyntaxLocationObject", ret = Void, args = {PyObject, Int, Int, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_SetImportError", ret = PyObject, args = {PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_SetImportErrorSubclass", ret = PyObject, args = {PyObject, PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_SetInterrupt", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_SetInterruptEx", ret = Int, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_SyntaxLocation", ret = Void, args = {ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_SyntaxLocationEx", ret = Void, args = {ConstCharPtrAsTruffleString, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_SyntaxLocationObject", ret = Void, args = {PyObject, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_WarnExplicit", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, Int, ConstCharPtrAsTruffleString, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_WarnExplicitFormat", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, Int, ConstCharPtrAsTruffleString, PyObject, ConstCharPtrAsTruffleString,
                    VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyErr_WarnExplicitObject", ret = Int, args = {PyObject, PyObject, PyObject, Int, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_AcquireLock", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_AcquireThread", ret = Void, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_CallFunction", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_CallMethod", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_EvalFrame", ret = PyObject, args = {PyFrameObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_EvalFrameEx", ret = PyObject, args = {PyFrameObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_GetFrame", ret = PyFrameObjectTransfer, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_GetFuncDesc", ret = ConstCharPtrAsTruffleString, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_GetFuncName", ret = ConstCharPtrAsTruffleString, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_GetGlobals", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_GetLocals", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_ReleaseLock", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_ReleaseThread", ret = Void, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_SetProfile", ret = Void, args = {PY_TRACEFUNC, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyEval_SetTrace", ret = Void, args = {PY_TRACEFUNC, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyException_GetCause", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyException_GetTraceback", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyExceptionClass_Name", ret = ConstCharPtrAsTruffleString, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFile_FromFd", ret = PyObject, args = {Int, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, Int, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString,
                    ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyFile_GetLine", ret = PyObject, args = {PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyFile_NewStdPrinter", ret = PyObject, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyFile_OpenCode", ret = PyObject, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyFile_OpenCodeObject", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFile_SetOpenCodeHook", ret = Int, args = {PY_OPENCODEHOOKFUNCTION, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "PyFloat_FromString", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFloat_GetInfo", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyFloat_GetMax", ret = Double, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyFloat_GetMin", ret = Double, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyFrame_BlockPop", ret = PyTryBlock, args = {PyFrameObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFrame_BlockSetup", ret = Void, args = {PyFrameObject, Int, Int, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyFrame_FastToLocals", ret = Void, args = {PyFrameObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFrame_FastToLocalsWithError", ret = Int, args = {PyFrameObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFrame_GetBack", ret = PyFrameObject, args = {PyFrameObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFrame_GetCode", ret = PyCodeObject, args = {PyFrameObjectTransfer}, call = NotImplemented)
    @CApiBuiltin(name = "PyFrame_GetLineNumber", ret = Int, args = {PyFrameObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFrame_LocalsToFast", ret = Void, args = {PyFrameObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_GetAnnotations", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_GetClosure", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_GetCode", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_GetDefaults", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_GetGlobals", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_GetKwDefaults", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_GetModule", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_New", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_NewWithQualName", ret = PyObject, args = {PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_SetAnnotations", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_SetClosure", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_SetDefaults", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyFunction_SetKwDefaults", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyGC_Collect", ret = Py_ssize_t, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyGC_Disable", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyGC_Enable", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyGC_IsEnabled", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyHash_GetFuncDef", ret = PYHASH_FUNCDEF_PTR, args = {}, call = Ignored, comment = "removed from our pyhash.h")
    @CApiBuiltin(name = "PyImport_AppendInittab", ret = Int, args = {ConstCharPtr, func_objvoid}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_ExecCodeModule", ret = PyObject, args = {ConstCharPtrAsTruffleString, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_ExecCodeModuleEx", ret = PyObject, args = {ConstCharPtrAsTruffleString, PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_ExecCodeModuleObject", ret = PyObject, args = {PyObject, PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_ExecCodeModuleWithPathnames", ret = PyObject, args = {ConstCharPtrAsTruffleString, PyObject, ConstCharPtrAsTruffleString,
                    ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_ExtendInittab", ret = Int, args = {INITTAB}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_GetImporter", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_GetMagicNumber", ret = Long, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_GetMagicTag", ret = ConstCharPtrAsTruffleString, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_GetModule", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_ImportFrozenModule", ret = Int, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_ImportFrozenModuleObject", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyImport_ReloadModule", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyInit__imp", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyInterpreterState_Clear", ret = Void, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "PyInterpreterState_Delete", ret = Void, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "PyInterpreterState_Get", ret = PyInterpreterState, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyInterpreterState_GetDict", ret = PyObject, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "PyInterpreterState_Head", ret = PyInterpreterState, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyInterpreterState_New", ret = PyInterpreterState, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyInterpreterState_Next", ret = PyInterpreterState, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "PyInterpreterState_ThreadHead", ret = PyThreadState, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "PyIter_Send", ret = PySendResult, args = {PyObject, PyObject, PyObjectPtr}, call = NotImplemented)
    @CApiBuiltin(name = "PyLineTable_InitAddressRange", ret = Void, args = {ConstCharPtr, Py_ssize_t, Int, PyCodeAddressRange}, call = NotImplemented)
    @CApiBuiltin(name = "PyLineTable_NextAddressRange", ret = Int, args = {PyCodeAddressRange}, call = NotImplemented)
    @CApiBuiltin(name = "PyLineTable_PreviousAddressRange", ret = Int, args = {PyCodeAddressRange}, call = NotImplemented)
    @CApiBuiltin(name = "PyLong_FromUnicodeObject", ret = PyObject, args = {PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyLong_GetInfo", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyMapping_HasKey", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyMapping_HasKeyString", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyMapping_Length", ret = Py_ssize_t, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyMapping_SetItemString", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyMem_GetAllocator", ret = Void, args = {PYMEMALLOCATORDOMAIN, PYMEMALLOCATOREX_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyMem_SetAllocator", ret = Void, args = {PYMEMALLOCATORDOMAIN, PYMEMALLOCATOREX_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyMem_SetupDebugHooks", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyMember_GetOne", ret = PyObject, args = {ConstCharPtrAsTruffleString, PyMemberDef}, call = NotImplemented)
    @CApiBuiltin(name = "PyMember_SetOne", ret = Int, args = {CHAR_PTR, PyMemberDef, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyModule_ExecDef", ret = Int, args = {PyObject, PyModuleDef}, call = NotImplemented)
    @CApiBuiltin(name = "PyModule_FromDefAndSpec2", ret = PyObject, args = {PyModuleDef, PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyModule_GetFilename", ret = ConstCharPtrAsTruffleString, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyModule_GetFilenameObject", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_AsCharBuffer", ret = Int, args = {PyObject, CONST_CHAR_PTR_LIST, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_AsReadBuffer", ret = Int, args = {PyObject, CONST_VOID_PTR_LIST, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_AsWriteBuffer", ret = Int, args = {PyObject, VOID_PTR_LIST, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_CallFinalizer", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_CallFinalizerFromDealloc", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_CallNoArgs", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_Calloc", ret = Pointer, args = {SIZE_T, SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_CheckReadBuffer", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_CopyData", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_DelItemString", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_GC_IsFinalized", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_GC_IsTracked", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_GET_WEAKREFS_LISTPTR", ret = PyObjectPtr, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_GetAIter", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_GetArenaAllocator", ret = Void, args = {PYOBJECTARENAALLOCATOR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_IS_GC", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_Length", ret = Py_ssize_t, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyObject_SetArenaAllocator", ret = Void, args = {PYOBJECTARENAALLOCATOR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyODict_DelItem", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyODict_New", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyODict_SetItem", ret = Int, args = {PyObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyOS_AfterFork_Child", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyOS_AfterFork_Parent", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyOS_AfterFork", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyOS_BeforeFork", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyOS_InterruptOccurred", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyOS_getsig", ret = PY_OS_SIGHANDLER, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyOS_setsig", ret = PY_OS_SIGHANDLER, args = {Int, PY_OS_SIGHANDLER}, call = NotImplemented)
    @CApiBuiltin(name = "PyOS_Readline", ret = CHAR_PTR, args = {FILE_PTR, FILE_PTR, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyPickleBuffer_FromObject", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyPickleBuffer_GetBuffer", ret = CONST_PY_BUFFER, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyPickleBuffer_Release", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyPreConfig_InitIsolatedConfig", ret = Void, args = {PYPRECONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyPreConfig_InitPythonConfig", ret = Void, args = {PYPRECONFIG_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_AnyFile", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_AnyFileEx", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_AnyFileExFlags", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString, Int, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_AnyFileFlags", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_File", ret = PyObject, args = {FILE_PTR, ConstCharPtrAsTruffleString, Int, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_FileEx", ret = PyObject, args = {FILE_PTR, ConstCharPtrAsTruffleString, Int, PyObject, PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_FileExFlags", ret = PyObject, args = {FILE_PTR, ConstCharPtrAsTruffleString, Int, PyObject, PyObject, Int, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_FileFlags", ret = PyObject, args = {FILE_PTR, ConstCharPtrAsTruffleString, Int, PyObject, PyObject, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_InteractiveLoop", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_InteractiveLoopFlags", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_InteractiveOne", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_InteractiveOneFlags", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_InteractiveOneObject", ret = Int, args = {FILE_PTR, PyObject, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_SimpleFile", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_SimpleFileEx", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_SimpleFileExFlags", ret = Int, args = {FILE_PTR, ConstCharPtrAsTruffleString, Int, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_SimpleString", ret = Int, args = {ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_SimpleStringFlags", ret = Int, args = {ConstCharPtrAsTruffleString, PY_COMPILER_FLAGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyRun_String", ret = PyObject, args = {ConstCharPtrAsTruffleString, Int, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PySequence_Count", ret = Py_ssize_t, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PySequence_DelSlice", ret = Int, args = {PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PySequence_In", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PySequence_Index", ret = Py_ssize_t, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PySequence_SetSlice", ret = Int, args = {PyObject, Py_ssize_t, Py_ssize_t, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PySignal_SetWakeupFd", ret = Int, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "PySlice_GetIndices", ret = Int, args = {PyObject, Py_ssize_t, PY_SSIZE_T_PTR, PY_SSIZE_T_PTR, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PySlice_GetIndicesEx", ret = Int, args = {PyObject, Py_ssize_t, PY_SSIZE_T_PTR, PY_SSIZE_T_PTR, PY_SSIZE_T_PTR, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyStatus_Error", ret = PYSTATUS, args = {ConstCharPtr}, call = NotImplemented)
    @CApiBuiltin(name = "PyStatus_Exception", ret = Int, args = {PYSTATUS}, call = NotImplemented)
    @CApiBuiltin(name = "PyStatus_Exit", ret = PYSTATUS, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyStatus_IsError", ret = Int, args = {PYSTATUS}, call = NotImplemented)
    @CApiBuiltin(name = "PyStatus_IsExit", ret = Int, args = {PYSTATUS}, call = NotImplemented)
    @CApiBuiltin(name = "PyStatus_NoMemory", ret = PYSTATUS, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyStatus_Ok", ret = PYSTATUS, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_AddAuditHook", ret = Int, args = {PY_AUDITHOOKFUNCTION, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_AddWarnOption", ret = Void, args = {CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_AddWarnOptionUnicode", ret = Void, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_AddXOption", ret = Void, args = {CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_FormatStderr", ret = Void, args = {ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_FormatStdout", ret = Void, args = {ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_GetXOptions", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_HasWarnOptions", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_ResetWarnOptions", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_SetArgv", ret = Void, args = {Int, WCHAR_T_PTR_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_SetArgvEx", ret = Void, args = {Int, WCHAR_T_PTR_LIST, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_SetObject", ret = Int, args = {ConstCharPtrAsTruffleString, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_SetPath", ret = Void, args = {CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_WriteStderr", ret = Void, args = {ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "PySys_WriteStdout", ret = Void, args = {ConstCharPtrAsTruffleString, VARARGS}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_acquire_lock_timed", ret = PY_LOCK_STATUS, args = {PY_THREAD_TYPE_LOCK, LONG_LONG, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_create_key", ret = Int, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_delete_key_value", ret = Void, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_delete_key", ret = Void, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_exit_thread", ret = VoidNoReturn, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_get_key_value", ret = Pointer, args = {Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_get_stacksize", ret = SIZE_T, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_get_thread_native_id", ret = UNSIGNED_LONG, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_GetInfo", ret = PyObject, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_init_thread", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_ReInitTLS", ret = Void, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_set_key_value", ret = Int, args = {Int, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_set_stacksize", ret = Int, args = {SIZE_T}, call = NotImplemented)
    @CApiBuiltin(name = "PyThread_start_new_thread", ret = UNSIGNED_LONG, args = {func_voidvoidptr, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "PyThreadState_Delete", ret = Void, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "PyThreadState_GetFrame", ret = PyFrameObjectTransfer, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "PyThreadState_GetID", ret = UINT64_T, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "PyThreadState_GetInterpreter", ret = PyInterpreterState, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "PyThreadState_New", ret = PyThreadState, args = {PyInterpreterState}, call = NotImplemented)
    @CApiBuiltin(name = "PyThreadState_Next", ret = PyThreadState, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "PyThreadState_SetAsyncExc", ret = Int, args = {UNSIGNED_LONG, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyThreadState_Swap", ret = PyThreadState, args = {PyThreadState}, call = NotImplemented)
    @CApiBuiltin(name = "PyTraceBack_Print", ret = Int, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyTruffle_SeqIter_New", ret = PyObjectTransfer, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyTruffleFrame_New", ret = PyFrameObjectTransfer, args = {PyThreadState, PyCodeObject, PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyType_ClearCache", ret = UNSIGNED_INT, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_AsCharmapString", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_AsDecodedObject", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_AsDecodedUnicode", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_AsEncodedObject", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_AsEncodedUnicode", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_AsRawUnicodeEscapeString", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_AsUTF16String", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_AsUTF32String", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_AsWideCharString", ret = WCHAR_T_PTR, args = {PyObject, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_BuildEncodingMap", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_CompareWithASCIIString", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_CopyCharacters", ret = Py_ssize_t, args = {PyObject, Py_ssize_t, PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_Count", ret = Py_ssize_t, args = {PyObject, PyObject, Py_ssize_t, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeCharmap", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeLocale", ret = PyObject, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeLocaleAndSize", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeRawUnicodeEscape", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeUnicodeEscape", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeUTF16", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString, INT_LIST}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeUTF16Stateful", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString, INT_LIST, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeUTF32Stateful", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString, INT_LIST, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeUTF7", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_DecodeUTF7Stateful", ret = PyObject, args = {ConstCharPtrAsTruffleString, Py_ssize_t, ConstCharPtrAsTruffleString, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_Encode", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeASCII", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeCharmap", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeDecimal", ret = Int, args = {PY_UNICODE_PTR, Py_ssize_t, CHAR_PTR, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeLatin1", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeLocale", ret = PyObject, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeRawUnicodeEscape", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeUnicodeEscape", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeUTF16", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeUTF32", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, ConstCharPtrAsTruffleString, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeUTF7", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, Int, Int, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_EncodeUTF8", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_Fill", ret = Py_ssize_t, args = {PyObject, Py_ssize_t, Py_ssize_t, PY_UCS4}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_Find", ret = Py_ssize_t, args = {PyObject, PyObject, Py_ssize_t, Py_ssize_t, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_FSDecoder", ret = Int, args = {PyObject, Pointer}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_GetDefaultEncoding", ret = ConstCharPtrAsTruffleString, args = {}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_GetSize", ret = Py_ssize_t, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_InternImmortal", ret = Void, args = {PyObjectPtr}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_IsIdentifier", ret = Int, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_Partition", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_Resize", ret = Int, args = {PyObjectPtr, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_RichCompare", ret = PyObject, args = {PyObject, PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_RPartition", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_RSplit", ret = PyObject, args = {PyObject, PyObject, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_Splitlines", ret = PyObject, args = {PyObject, Int}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_TransformDecimalToASCII", ret = PyObject, args = {PY_UNICODE_PTR, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_Translate", ret = PyObject, args = {PyObject, PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_TranslateCharmap", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicode_WriteChar", ret = Int, args = {PyObject, Py_ssize_t, PY_UCS4}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeDecodeError_Create", ret = PyObject, args = {ConstCharPtrAsTruffleString, ConstCharPtrAsTruffleString, Py_ssize_t, Py_ssize_t, Py_ssize_t,
                    ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeDecodeError_GetEncoding", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeDecodeError_GetEnd", ret = Int, args = {PyObject, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeDecodeError_GetObject", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeDecodeError_GetReason", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeDecodeError_GetStart", ret = Int, args = {PyObject, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeDecodeError_SetEnd", ret = Int, args = {PyObject, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeDecodeError_SetReason", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeDecodeError_SetStart", ret = Int, args = {PyObject, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeEncodeError_Create", ret = PyObject, args = {ConstCharPtrAsTruffleString, CONST_PY_UNICODE, Py_ssize_t, Py_ssize_t, Py_ssize_t,
                    ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeEncodeError_GetEncoding", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeEncodeError_GetEnd", ret = Int, args = {PyObject, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeEncodeError_GetObject", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeEncodeError_GetReason", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeEncodeError_GetStart", ret = Int, args = {PyObject, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeEncodeError_SetEnd", ret = Int, args = {PyObject, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeEncodeError_SetReason", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeEncodeError_SetStart", ret = Int, args = {PyObject, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeTranslateError_Create", ret = PyObject, args = {CONST_PY_UNICODE, Py_ssize_t, Py_ssize_t, Py_ssize_t, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeTranslateError_GetEnd", ret = Int, args = {PyObject, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeTranslateError_GetObject", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeTranslateError_GetReason", ret = PyObject, args = {PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeTranslateError_GetStart", ret = Int, args = {PyObject, PY_SSIZE_T_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeTranslateError_SetEnd", ret = Int, args = {PyObject, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeTranslateError_SetReason", ret = Int, args = {PyObject, ConstCharPtrAsTruffleString}, call = NotImplemented)
    @CApiBuiltin(name = "PyUnicodeTranslateError_SetStart", ret = Int, args = {PyObject, Py_ssize_t}, call = NotImplemented)
    @CApiBuiltin(name = "PyWeakref_NewProxy", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)
    @CApiBuiltin(name = "PyWideStringList_Append", ret = PYSTATUS, args = {PYWIDESTRINGLIST_PTR, CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyWideStringList_Insert", ret = PYSTATUS, args = {PYWIDESTRINGLIST_PTR, Py_ssize_t, CONST_WCHAR_PTR}, call = NotImplemented)
    @CApiBuiltin(name = "PyWrapper_New", ret = PyObject, args = {PyObject, PyObject}, call = NotImplemented)

    private static final class Dummy {
        // only here for the annotations
    }

    static List<CApiBuiltinDesc> getOtherBuiltinDefinitions() {
        ArrayList<CApiBuiltinDesc> result = new ArrayList<>();
        CApiBuiltins builtins = Dummy.class.getAnnotation(CApiBuiltins.class);
        for (var builtin : builtins.value()) {
            result.add(new CApiBuiltinDesc(builtin.name(), builtin.inlined(), builtin.ret(), builtin.args(), builtin.call(), builtin.forwardsTo(), null));
        }
        return result;
    }

    static List<CApiBuiltinDesc> getJavaBuiltinDefinitions() {
        ArrayList<CApiBuiltinDesc> result = new ArrayList<>();
        addCApiBuiltins(result, PythonCextAbstractBuiltins.class);
        addCApiBuiltins(result, PythonCextBoolBuiltins.class);
        addCApiBuiltins(result, PythonCextBuiltins.class);
        addCApiBuiltins(result, PythonCextBytesBuiltins.class);
        addCApiBuiltins(result, PythonCextCapsuleBuiltins.class);
        addCApiBuiltins(result, PythonCextCEvalBuiltins.class);
        addCApiBuiltins(result, PythonCextClassBuiltins.class);
        addCApiBuiltins(result, PythonCextCodeBuiltins.class);
        addCApiBuiltins(result, PythonCextComplexBuiltins.class);
        addCApiBuiltins(result, PythonCextContextBuiltins.class);
        addCApiBuiltins(result, PythonCextDateTimeBuiltins.class);
        addCApiBuiltins(result, PythonCextDescrBuiltins.class);
        addCApiBuiltins(result, PythonCextDictBuiltins.class);
        addCApiBuiltins(result, PythonCextErrBuiltins.class);
        addCApiBuiltins(result, PythonCextFileBuiltins.class);
        addCApiBuiltins(result, PythonCextFloatBuiltins.class);
        addCApiBuiltins(result, PythonCextFuncBuiltins.class);
        addCApiBuiltins(result, PythonCextGenericAliasBuiltins.class);
        addCApiBuiltins(result, PythonCextHashBuiltins.class);
        addCApiBuiltins(result, PythonCextImportBuiltins.class);
        addCApiBuiltins(result, PythonCextIterBuiltins.class);
        addCApiBuiltins(result, PythonCextListBuiltins.class);
        addCApiBuiltins(result, PythonCextLongBuiltins.class);
        addCApiBuiltins(result, PythonCextMemoryViewBuiltins.class);
        addCApiBuiltins(result, PythonCextMethodBuiltins.class);
        addCApiBuiltins(result, PythonCextModuleBuiltins.class);
        addCApiBuiltins(result, PythonCextNamespaceBuiltins.class);
        addCApiBuiltins(result, PythonCextObjectBuiltins.class);
        addCApiBuiltins(result, PythonCextPosixmoduleBuiltins.class);
        addCApiBuiltins(result, PythonCextPyLifecycleBuiltins.class);
        addCApiBuiltins(result, PythonCextPyStateBuiltins.class);
        addCApiBuiltins(result, PythonCextPyThreadBuiltins.class);
        addCApiBuiltins(result, PythonCextPythonRunBuiltins.class);
        addCApiBuiltins(result, PythonCextSetBuiltins.class);
        addCApiBuiltins(result, PythonCextSliceBuiltins.class);
        addCApiBuiltins(result, PythonCextSlotBuiltins.class);
        addCApiBuiltins(result, PythonCextStructSeqBuiltins.class);
        addCApiBuiltins(result, PythonCextSysBuiltins.class);
        addCApiBuiltins(result, PythonCextTracebackBuiltins.class);
        addCApiBuiltins(result, PythonCextTupleBuiltins.class);
        addCApiBuiltins(result, PythonCextTypeBuiltins.class);
        addCApiBuiltins(result, PythonCextUnicodeBuiltins.class);
        addCApiBuiltins(result, PythonCextWarnBuiltins.class);
        addCApiBuiltins(result, PythonCextWeakrefBuiltins.class);

        // sorting for consistent results (minimal diff when adding/removing builtins)
        result.sort((a, b) -> a.name.compareTo(b.name));
        for (int i = 0; i < result.size(); i++) {
            result.get(i).id = i;
        }
        return result;
    }

    private static void addCApiBuiltins(List<CApiBuiltinDesc> result, Class<?> container) {
        Class<?>[] declaredClasses = container.getDeclaredClasses();

        for (Class<?> clazz : declaredClasses) {
            if (CApiBuiltinNode.class.isAssignableFrom(clazz)) {
                CApiBuiltins builtins = clazz.getAnnotation(CApiBuiltins.class);
                CApiBuiltin[] annotations;
                if (builtins == null) {
                    CApiBuiltin annotation = clazz.getAnnotation(CApiBuiltin.class);
                    if (annotation == null) {
                        // not builtin, but base class
                        continue;
                    }
                    annotations = new CApiBuiltin[]{annotation};
                } else {
                    annotations = builtins.value();
                }
                try {
                    for (var annotation : annotations) {
                        String name = clazz.getSimpleName();
                        if (!annotation.name().isEmpty()) {
                            name = annotation.name();
                        }
                        Class<?> gen;
                        try {
                            gen = Class.forName(container.getName() + "Factory$" + clazz.getSimpleName() + "NodeGen");
                        } catch (ClassNotFoundException e) {
                            try {
                                // in case this uses GenerateNodeFactory:
                                gen = Class.forName(container.getName() + "Factory$" + clazz.getSimpleName() + "Factory");
                            } catch (ClassNotFoundException e2) {
                                throw new RuntimeException(e2);
                            }
                        }
                        verifyNodeClass(clazz, annotation);
                        result.add(new CApiBuiltinDesc(name, annotation.inlined(), annotation.ret(), annotation.args(), annotation.call(), annotation.forwardsTo(), gen.getCanonicalName()));
                    }
                } catch (Throwable t) {
                    throw new RuntimeException("while processing " + clazz, t);
                }
            }
        }
    }

    private static void verifyNodeClass(Class<?> clazz, CApiBuiltin annotation) {
        for (Method method : clazz.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers()) && "execute".equals(method.getName())) {
                if (method.getParameterTypes().length != annotation.args().length) {
                    throw new AssertionError("Arity mismatch between declared arguments and builtin superclass for " + clazz.getName());
                }
                return;
            }
        }
        throw new RuntimeException("Couldn't find execute method for C builtin " + clazz.getName());
    }
}
