/*
 * Copyright (c) 2018, 2022, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.graal.python.nodes.bytecode;

import static com.oracle.graal.python.builtins.PythonBuiltinClassType.RecursionError;
import static com.oracle.graal.python.builtins.PythonBuiltinClassType.SystemError;
import static com.oracle.graal.python.builtins.PythonBuiltinClassType.ZeroDivisionError;
import static com.oracle.graal.python.nodes.BuiltinNames.T___BUILD_CLASS__;
import static com.oracle.graal.python.nodes.SpecialAttributeNames.T___CLASS__;
import static com.oracle.graal.python.util.PythonUtils.TS_ENCODING;
import static com.oracle.graal.python.util.PythonUtils.toTruffleStringUncached;

import java.math.BigInteger;
import java.util.Arrays;

import com.oracle.graal.python.PythonLanguage;
import com.oracle.graal.python.builtins.PythonBuiltinClassType;
import com.oracle.graal.python.builtins.modules.BuiltinFunctions.FormatNode;
import com.oracle.graal.python.builtins.modules.BuiltinFunctionsFactory.FormatNodeFactory.FormatNodeGen;
import com.oracle.graal.python.builtins.modules.MarshalModuleBuiltins;
import com.oracle.graal.python.builtins.objects.PNone;
import com.oracle.graal.python.builtins.objects.cell.PCell;
import com.oracle.graal.python.builtins.objects.common.HashingCollectionNodes;
import com.oracle.graal.python.builtins.objects.common.HashingCollectionNodes.SetItemNode;
import com.oracle.graal.python.builtins.objects.common.HashingCollectionNodesFactory;
import com.oracle.graal.python.builtins.objects.common.HashingStorage;
import com.oracle.graal.python.builtins.objects.common.HashingStorageFactory;
import com.oracle.graal.python.builtins.objects.dict.DictNodes;
import com.oracle.graal.python.builtins.objects.dict.DictNodesFactory;
import com.oracle.graal.python.builtins.objects.dict.PDict;
import com.oracle.graal.python.builtins.objects.ellipsis.PEllipsis;
import com.oracle.graal.python.builtins.objects.exception.PBaseException;
import com.oracle.graal.python.builtins.objects.frame.PFrame;
import com.oracle.graal.python.builtins.objects.function.PArguments;
import com.oracle.graal.python.builtins.objects.function.PKeyword;
import com.oracle.graal.python.builtins.objects.function.Signature;
import com.oracle.graal.python.builtins.objects.generator.GeneratorControlData;
import com.oracle.graal.python.builtins.objects.generator.ThrowData;
import com.oracle.graal.python.builtins.objects.ints.IntBuiltins;
import com.oracle.graal.python.builtins.objects.ints.IntBuiltinsFactory;
import com.oracle.graal.python.builtins.objects.list.ListBuiltins;
import com.oracle.graal.python.builtins.objects.list.ListBuiltinsFactory;
import com.oracle.graal.python.builtins.objects.list.PList;
import com.oracle.graal.python.builtins.objects.set.PSet;
import com.oracle.graal.python.builtins.objects.set.SetBuiltins;
import com.oracle.graal.python.builtins.objects.set.SetBuiltinsFactory;
import com.oracle.graal.python.builtins.objects.set.SetNodes;
import com.oracle.graal.python.builtins.objects.set.SetNodesFactory;
import com.oracle.graal.python.builtins.objects.slice.PSlice;
import com.oracle.graal.python.builtins.objects.slice.SliceNodes.CreateSliceNode;
import com.oracle.graal.python.builtins.objects.slice.SliceNodesFactory.CreateSliceNodeGen;
import com.oracle.graal.python.compiler.BinaryOpsConstants;
import com.oracle.graal.python.compiler.CodeUnit;
import com.oracle.graal.python.compiler.FormatOptions;
import com.oracle.graal.python.compiler.OpCodes;
import com.oracle.graal.python.compiler.OpCodes.CollectionBits;
import com.oracle.graal.python.compiler.OpCodesConstants;
import com.oracle.graal.python.compiler.QuickeningTypes;
import com.oracle.graal.python.compiler.UnaryOpsConstants;
import com.oracle.graal.python.lib.PyObjectAsciiNode;
import com.oracle.graal.python.lib.PyObjectAsciiNodeGen;
import com.oracle.graal.python.lib.PyObjectDelItem;
import com.oracle.graal.python.lib.PyObjectDelItemNodeGen;
import com.oracle.graal.python.lib.PyObjectGetAttr;
import com.oracle.graal.python.lib.PyObjectGetAttrNodeGen;
import com.oracle.graal.python.lib.PyObjectGetIter;
import com.oracle.graal.python.lib.PyObjectGetIterNodeGen;
import com.oracle.graal.python.lib.PyObjectGetMethod;
import com.oracle.graal.python.lib.PyObjectGetMethodNodeGen;
import com.oracle.graal.python.lib.PyObjectIsTrueNode;
import com.oracle.graal.python.lib.PyObjectIsTrueNodeGen;
import com.oracle.graal.python.lib.PyObjectReprAsObjectNode;
import com.oracle.graal.python.lib.PyObjectReprAsObjectNodeGen;
import com.oracle.graal.python.lib.PyObjectSetAttr;
import com.oracle.graal.python.lib.PyObjectSetAttrNodeGen;
import com.oracle.graal.python.lib.PyObjectSetItem;
import com.oracle.graal.python.lib.PyObjectSetItemNodeGen;
import com.oracle.graal.python.lib.PyObjectStrAsObjectNode;
import com.oracle.graal.python.lib.PyObjectStrAsObjectNodeGen;
import com.oracle.graal.python.nodes.ErrorMessages;
import com.oracle.graal.python.nodes.PRaiseNode;
import com.oracle.graal.python.nodes.PRaiseNodeGen;
import com.oracle.graal.python.nodes.PRootNode;
import com.oracle.graal.python.nodes.argument.positional.ExecutePositionalStarargsNode;
import com.oracle.graal.python.nodes.argument.positional.ExecutePositionalStarargsNodeGen;
import com.oracle.graal.python.nodes.builtins.ListNodes;
import com.oracle.graal.python.nodes.builtins.ListNodesFactory;
import com.oracle.graal.python.nodes.builtins.TupleNodes;
import com.oracle.graal.python.nodes.builtins.TupleNodesFactory;
import com.oracle.graal.python.nodes.call.CallNode;
import com.oracle.graal.python.nodes.call.CallNodeGen;
import com.oracle.graal.python.nodes.call.special.CallBinaryMethodNode;
import com.oracle.graal.python.nodes.call.special.CallBinaryMethodNodeGen;
import com.oracle.graal.python.nodes.call.special.CallQuaternaryMethodNode;
import com.oracle.graal.python.nodes.call.special.CallQuaternaryMethodNodeGen;
import com.oracle.graal.python.nodes.call.special.CallTernaryMethodNode;
import com.oracle.graal.python.nodes.call.special.CallTernaryMethodNodeGen;
import com.oracle.graal.python.nodes.call.special.CallUnaryMethodNode;
import com.oracle.graal.python.nodes.call.special.CallUnaryMethodNodeGen;
import com.oracle.graal.python.nodes.expression.BinaryArithmetic;
import com.oracle.graal.python.nodes.expression.BinaryComparisonNode;
import com.oracle.graal.python.nodes.expression.BinaryOp;
import com.oracle.graal.python.nodes.expression.CoerceToBooleanNode;
import com.oracle.graal.python.nodes.expression.ContainsNode;
import com.oracle.graal.python.nodes.expression.InplaceArithmetic;
import com.oracle.graal.python.nodes.expression.UnaryArithmetic.InvertNode;
import com.oracle.graal.python.nodes.expression.UnaryArithmetic.NegNode;
import com.oracle.graal.python.nodes.expression.UnaryArithmetic.PosNode;
import com.oracle.graal.python.nodes.expression.UnaryOpNode;
import com.oracle.graal.python.nodes.frame.DeleteGlobalNode;
import com.oracle.graal.python.nodes.frame.DeleteGlobalNodeGen;
import com.oracle.graal.python.nodes.frame.ReadGlobalOrBuiltinNode;
import com.oracle.graal.python.nodes.frame.ReadGlobalOrBuiltinNodeGen;
import com.oracle.graal.python.nodes.frame.ReadNameNode;
import com.oracle.graal.python.nodes.frame.ReadNameNodeGen;
import com.oracle.graal.python.nodes.frame.WriteGlobalNode;
import com.oracle.graal.python.nodes.frame.WriteGlobalNodeGen;
import com.oracle.graal.python.nodes.frame.WriteNameNode;
import com.oracle.graal.python.nodes.frame.WriteNameNodeGen;
import com.oracle.graal.python.nodes.object.IsNode;
import com.oracle.graal.python.nodes.statement.ExceptNode.ExceptMatchNode;
import com.oracle.graal.python.nodes.statement.ExceptNodeFactory.ExceptMatchNodeGen;
import com.oracle.graal.python.nodes.statement.ExceptionHandlingStatementNode;
import com.oracle.graal.python.nodes.statement.ImportStarNode;
import com.oracle.graal.python.nodes.statement.RaiseNode;
import com.oracle.graal.python.nodes.statement.RaiseNodeGen;
import com.oracle.graal.python.nodes.subscript.DeleteItemNode;
import com.oracle.graal.python.nodes.subscript.DeleteItemNodeGen;
import com.oracle.graal.python.nodes.subscript.GetItemNode;
import com.oracle.graal.python.nodes.subscript.GetItemNodeGen;
import com.oracle.graal.python.nodes.util.CastToJavaIntExactNode;
import com.oracle.graal.python.nodes.util.CastToJavaIntExactNodeGen;
import com.oracle.graal.python.nodes.util.ExceptionStateNodes;
import com.oracle.graal.python.parser.GeneratorInfo;
import com.oracle.graal.python.runtime.ExecutionContext.CalleeContext;
import com.oracle.graal.python.runtime.PythonContext;
import com.oracle.graal.python.runtime.PythonOptions;
import com.oracle.graal.python.runtime.exception.PException;
import com.oracle.graal.python.runtime.exception.PythonErrorType;
import com.oracle.graal.python.runtime.exception.PythonExitException;
import com.oracle.graal.python.runtime.object.PythonObjectFactory;
import com.oracle.graal.python.util.OverflowException;
import com.oracle.graal.python.util.PythonUtils;
import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.CompilerDirectives.ValueType;
import com.oracle.truffle.api.HostCompilerDirectives.BytecodeInterpreterSwitch;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.BytecodeOSRNode;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;

/**
 * Root node with main bytecode interpreter loop.
 */
public final class PBytecodeRootNode extends PRootNode implements BytecodeOSRNode {

    private static final NodeSupplier<RaiseNode> NODE_RAISENODE = () -> RaiseNode.create(null, null);
    private static final NodeSupplier<DeleteItemNode> NODE_DELETE_ITEM = DeleteItemNode::create;
    private static final NodeSupplier<PyObjectDelItem> NODE_OBJECT_DEL_ITEM = PyObjectDelItem::create;
    private static final PyObjectDelItem UNCACHED_OBJECT_DEL_ITEM = PyObjectDelItem.getUncached();

    private static final NodeSupplier<SetItemNode> NODE_SET_ITEM = HashingCollectionNodes.SetItemNode::create;
    private static final SetItemNode UNCACHED_SET_ITEM = HashingCollectionNodes.SetItemNode.getUncached();
    private static final NodeSupplier<CastToJavaIntExactNode> NODE_CAST_TO_JAVA_INT_EXACT = CastToJavaIntExactNode::create;
    private static final CastToJavaIntExactNode UNCACHED_CAST_TO_JAVA_INT_EXACT = CastToJavaIntExactNode.getUncached();
    private static final NodeSupplier<ImportNode> NODE_IMPORT = ImportNode::new;
    private static final NodeSupplier<PyObjectGetAttr> NODE_OBJECT_GET_ATTR = PyObjectGetAttr::create;
    private static final PyObjectGetAttr UNCACHED_OBJECT_GET_ATTR = PyObjectGetAttr.getUncached();
    private static final NodeSupplier<PRaiseNode> NODE_RAISE = PRaiseNode::create;
    private static final PRaiseNode UNCACHED_RAISE = PRaiseNode.getUncached();
    private static final NodeSupplier<CallNode> NODE_CALL = CallNode::create;
    private static final CallNode UNCACHED_CALL = CallNode.getUncached();
    private static final NodeSupplier<CallQuaternaryMethodNode> NODE_CALL_QUATERNARY_METHOD = CallQuaternaryMethodNode::create;
    private static final CallQuaternaryMethodNode UNCACHED_CALL_QUATERNARY_METHOD = CallQuaternaryMethodNode.getUncached();
    private static final NodeSupplier<CallTernaryMethodNode> NODE_CALL_TERNARY_METHOD = CallTernaryMethodNode::create;
    private static final CallTernaryMethodNode UNCACHED_CALL_TERNARY_METHOD = CallTernaryMethodNode.getUncached();
    private static final NodeSupplier<CallBinaryMethodNode> NODE_CALL_BINARY_METHOD = CallBinaryMethodNode::create;
    private static final CallBinaryMethodNode UNCACHED_CALL_BINARY_METHOD = CallBinaryMethodNode.getUncached();
    private static final NodeSupplier<CallUnaryMethodNode> NODE_CALL_UNARY_METHOD = CallUnaryMethodNode::create;
    private static final CallUnaryMethodNode UNCACHED_CALL_UNARY_METHOD = CallUnaryMethodNode.getUncached();
    private static final NodeSupplier<PyObjectGetMethod> NODE_OBJECT_GET_METHOD = PyObjectGetMethodNodeGen::create;
    private static final PyObjectGetMethod UNCACHED_OBJECT_GET_METHOD = PyObjectGetMethodNodeGen.getUncached();
    private static final ForIterONode UNCACHED_FOR_ITER_O = ForIterONode.getUncached();
    private static final NodeSupplier<ForIterONode> NODE_FOR_ITER_O = ForIterONode::create;
    private static final ForIterINode UNCACHED_FOR_ITER_I = ForIterINode.getUncached();
    private static final NodeSupplier<ForIterINode> NODE_FOR_ITER_I = ForIterINode::create;
    private static final NodeSupplier<PyObjectGetIter> NODE_OBJECT_GET_ITER = PyObjectGetIter::create;
    private static final PyObjectGetIter UNCACHED_OBJECT_GET_ITER = PyObjectGetIter.getUncached();
    private static final NodeSupplier<PyObjectSetAttr> NODE_OBJECT_SET_ATTR = PyObjectSetAttr::create;
    private static final PyObjectSetAttr UNCACHED_OBJECT_SET_ATTR = PyObjectSetAttr.getUncached();
    private static final NodeSupplier<ReadGlobalOrBuiltinNode> NODE_READ_GLOBAL_OR_BUILTIN_BUILD_CLASS = () -> ReadGlobalOrBuiltinNode.create(T___BUILD_CLASS__);
    private static final NodeFunction<TruffleString, ReadGlobalOrBuiltinNode> NODE_READ_GLOBAL_OR_BUILTIN = ReadGlobalOrBuiltinNode::create;
    private static final NodeFunction<TruffleString, ReadNameNode> NODE_READ_NAME = ReadNameNode::create;
    private static final NodeFunction<TruffleString, WriteNameNode> NODE_WRITE_NAME = WriteNameNode::create;
    private static final ReadGlobalOrBuiltinNode UNCACHED_READ_GLOBAL_OR_BUILTIN = ReadGlobalOrBuiltinNode.getUncached();
    private static final NodeSupplier<PyObjectSetItem> NODE_OBJECT_SET_ITEM = PyObjectSetItem::create;
    private static final PyObjectSetItem UNCACHED_OBJECT_SET_ITEM = PyObjectSetItem.getUncached();
    private static final NodeSupplier<PyObjectIsTrueNode> NODE_OBJECT_IS_TRUE = PyObjectIsTrueNode::create;
    private static final PyObjectIsTrueNode UNCACHED_OBJECT_IS_TRUE = PyObjectIsTrueNode.getUncached();
    private static final NodeSupplier<GetItemNode> NODE_GET_ITEM = GetItemNode::create;
    private static final ExceptMatchNode UNCACHED_EXCEPT_MATCH = ExceptMatchNode.getUncached();
    private static final NodeSupplier<ExceptMatchNode> NODE_EXCEPT_MATCH = ExceptMatchNode::create;
    private static final SetupWithNode UNCACHED_SETUP_WITH_NODE = SetupWithNode.getUncached();
    private static final NodeSupplier<SetupWithNode> NODE_SETUP_WITH = SetupWithNode::create;
    private static final ExitWithNode UNCACHED_EXIT_WITH_NODE = ExitWithNode.getUncached();
    private static final NodeSupplier<ExitWithNode> NODE_EXIT_WITH = ExitWithNode::create;
    private static final ImportFromNode UNCACHED_IMPORT_FROM = ImportFromNode.getUncached();
    private static final NodeSupplier<ImportFromNode> NODE_IMPORT_FROM = ImportFromNode::create;
    private static final ExecutePositionalStarargsNode UNCACHED_EXECUTE_STARARGS = ExecutePositionalStarargsNode.getUncached();
    private static final NodeSupplier<ExecutePositionalStarargsNode> NODE_EXECUTE_STARARGS = ExecutePositionalStarargsNode::create;
    private static final KeywordsNode UNCACHED_KEYWORDS = KeywordsNode.getUncached();
    private static final NodeSupplier<KeywordsNode> NODE_KEYWORDS = KeywordsNode::create;
    private static final CreateSliceNode UNCACHED_CREATE_SLICE = CreateSliceNode.getUncached();
    private static final NodeSupplier<CreateSliceNode> NODE_CREATE_SLICE = CreateSliceNode::create;
    private static final ListNodes.ConstructListNode UNCACHED_CONSTRUCT_LIST = ListNodes.ConstructListNode.getUncached();
    private static final NodeSupplier<ListNodes.ConstructListNode> NODE_CONSTRUCT_LIST = ListNodes.ConstructListNode::create;
    private static final TupleNodes.ConstructTupleNode UNCACHED_CONSTRUCT_TUPLE = TupleNodes.ConstructTupleNode.getUncached();
    private static final NodeSupplier<TupleNodes.ConstructTupleNode> NODE_CONSTRUCT_TUPLE = TupleNodes.ConstructTupleNode::create;
    private static final SetNodes.ConstructSetNode UNCACHED_CONSTRUCT_SET = SetNodes.ConstructSetNode.getUncached();
    private static final NodeSupplier<SetNodes.ConstructSetNode> NODE_CONSTRUCT_SET = SetNodes.ConstructSetNode::create;
    private static final NodeSupplier<HashingStorage.InitNode> NODE_HASHING_STORAGE_INIT = HashingStorage.InitNode::create;
    private static final NodeSupplier<ListBuiltins.ListExtendNode> NODE_LIST_EXTEND = ListBuiltins.ListExtendNode::create;
    private static final SetBuiltins.UpdateSingleNode UNCACHED_SET_UPDATE = SetBuiltins.UpdateSingleNode.getUncached();
    private static final NodeSupplier<DictNodes.UpdateNode> NODE_DICT_UPDATE = DictNodes.UpdateNode::create;
    private static final NodeSupplier<SetBuiltins.UpdateSingleNode> NODE_SET_UPDATE = SetBuiltins.UpdateSingleNode::create;
    private static final ListNodes.AppendNode UNCACHED_LIST_APPEND = ListNodes.AppendNode.getUncached();
    private static final NodeSupplier<ListNodes.AppendNode> NODE_LIST_APPEND = ListNodes.AppendNode::create;
    private static final SetNodes.AddNode UNCACHED_SET_ADD = SetNodes.AddNode.getUncached();
    private static final NodeSupplier<SetNodes.AddNode> NODE_SET_ADD = SetNodes.AddNode::create;
    private static final KwargsMergeNode UNCACHED_KWARGS_MERGE = KwargsMergeNode.getUncached();
    private static final NodeSupplier<KwargsMergeNode> NODE_KWARGS_MERGE = KwargsMergeNode::create;
    private static final UnpackSequenceNode UNCACHED_UNPACK_SEQUENCE = UnpackSequenceNode.getUncached();
    private static final NodeSupplier<UnpackSequenceNode> NODE_UNPACK_SEQUENCE = UnpackSequenceNode::create;
    private static final UnpackExNode UNCACHED_UNPACK_EX = UnpackExNode.getUncached();
    private static final NodeSupplier<UnpackExNode> NODE_UNPACK_EX = UnpackExNode::create;
    private static final PyObjectStrAsObjectNode UNCACHED_STR = PyObjectStrAsObjectNode.getUncached();
    private static final NodeSupplier<PyObjectStrAsObjectNode> NODE_STR = PyObjectStrAsObjectNode::create;
    private static final PyObjectReprAsObjectNode UNCACHED_REPR = PyObjectReprAsObjectNode.getUncached();
    private static final NodeSupplier<PyObjectReprAsObjectNode> NODE_REPR = PyObjectReprAsObjectNode::create;
    private static final PyObjectAsciiNode UNCACHED_ASCII = PyObjectAsciiNode.getUncached();
    private static final NodeSupplier<PyObjectAsciiNode> NODE_ASCII = PyObjectAsciiNode::create;
    private static final NodeSupplier<FormatNode> NODE_FORMAT = FormatNode::create;
    private static final NodeSupplier<SendNode> NODE_SEND = SendNode::create;
    private static final NodeSupplier<ThrowNode> NODE_THROW = ThrowNode::create;
    private static final WriteGlobalNode UNCACHED_WRITE_GLOBAL = WriteGlobalNode.getUncached();
    private static final NodeFunction<TruffleString, WriteGlobalNode> NODE_WRITE_GLOBAL = WriteGlobalNode::create;
    private static final NodeFunction<TruffleString, DeleteGlobalNode> NODE_DELETE_GLOBAL = DeleteGlobalNode::create;
    private static final PrintExprNode UNCACHED_PRINT_EXPR = PrintExprNode.getUncached();
    private static final NodeSupplier<PrintExprNode> NODE_PRINT_EXPR = PrintExprNode::create;
    private static final GetNameFromLocalsNode UNCACHED_GET_NAME_FROM_LOCALS = GetNameFromLocalsNode.getUncached();
    private static final NodeSupplier<GetNameFromLocalsNode> NODE_GET_NAME_FROM_LOCALS = GetNameFromLocalsNode::create;
    private static final SetupAnnotationsNode UNCACHED_SETUP_ANNOTATIONS = SetupAnnotationsNode.getUncached();
    private static final NodeSupplier<SetupAnnotationsNode> NODE_SETUP_ANNOTATIONS = SetupAnnotationsNode::create;

    private static final NodeSupplier<IntBuiltins.AddNode> NODE_INT_ADD = IntBuiltins.AddNode::create;
    private static final NodeSupplier<IntBuiltins.SubNode> NODE_INT_SUB = IntBuiltins.SubNode::create;
    private static final NodeSupplier<IntBuiltins.MulNode> NODE_INT_MUL = IntBuiltins.MulNode::create;
    private static final NodeSupplier<IntBuiltins.FloorDivNode> NODE_INT_FLOORDIV = IntBuiltins.FloorDivNode::create;
    private static final NodeSupplier<IntBuiltins.TrueDivNode> NODE_INT_TRUEDIV = IntBuiltins.TrueDivNode::create;
    private static final NodeSupplier<IntBuiltins.ModNode> NODE_INT_MOD = IntBuiltins.ModNode::create;
    private static final NodeSupplier<IntBuiltins.LShiftNode> NODE_INT_LSHIFT = IntBuiltins.LShiftNode::create;
    private static final NodeSupplier<IntBuiltins.RShiftNode> NODE_INT_RSHIFT = IntBuiltins.RShiftNode::create;

    private static final IntNodeFunction<UnaryOpNode> UNARY_OP_FACTORY = (int op) -> {
        switch (op) {
            case UnaryOpsConstants.NOT:
                return CoerceToBooleanNode.createIfFalseNode();
            case UnaryOpsConstants.POSITIVE:
                return PosNode.create();
            case UnaryOpsConstants.NEGATIVE:
                return NegNode.create();
            case UnaryOpsConstants.INVERT:
                return InvertNode.create();
            default:
                throw CompilerDirectives.shouldNotReachHere();
        }
    };

    private static final IntNodeFunction<Node> BINARY_OP_FACTORY = (int op) -> {
        switch (op) {
            case BinaryOpsConstants.ADD:
                return BinaryArithmetic.Add.create();
            case BinaryOpsConstants.SUB:
                return BinaryArithmetic.Sub.create();
            case BinaryOpsConstants.MUL:
                return BinaryArithmetic.Mul.create();
            case BinaryOpsConstants.TRUEDIV:
                return BinaryArithmetic.TrueDiv.create();
            case BinaryOpsConstants.FLOORDIV:
                return BinaryArithmetic.FloorDiv.create();
            case BinaryOpsConstants.MOD:
                return BinaryArithmetic.Mod.create();
            case BinaryOpsConstants.LSHIFT:
                return BinaryArithmetic.LShift.create();
            case BinaryOpsConstants.RSHIFT:
                return BinaryArithmetic.RShift.create();
            case BinaryOpsConstants.AND:
                return BinaryArithmetic.And.create();
            case BinaryOpsConstants.OR:
                return BinaryArithmetic.Or.create();
            case BinaryOpsConstants.XOR:
                return BinaryArithmetic.Xor.create();
            case BinaryOpsConstants.POW:
                return BinaryArithmetic.Pow.create();
            case BinaryOpsConstants.MATMUL:
                return BinaryArithmetic.MatMul.create();
            case BinaryOpsConstants.INPLACE_ADD:
                return InplaceArithmetic.IAdd.create();
            case BinaryOpsConstants.INPLACE_SUB:
                return InplaceArithmetic.ISub.create();
            case BinaryOpsConstants.INPLACE_MUL:
                return InplaceArithmetic.IMul.create();
            case BinaryOpsConstants.INPLACE_TRUEDIV:
                return InplaceArithmetic.ITrueDiv.create();
            case BinaryOpsConstants.INPLACE_FLOORDIV:
                return InplaceArithmetic.IFloorDiv.create();
            case BinaryOpsConstants.INPLACE_MOD:
                return InplaceArithmetic.IMod.create();
            case BinaryOpsConstants.INPLACE_LSHIFT:
                return InplaceArithmetic.ILShift.create();
            case BinaryOpsConstants.INPLACE_RSHIFT:
                return InplaceArithmetic.IRShift.create();
            case BinaryOpsConstants.INPLACE_AND:
                return InplaceArithmetic.IAnd.create();
            case BinaryOpsConstants.INPLACE_OR:
                return InplaceArithmetic.IOr.create();
            case BinaryOpsConstants.INPLACE_XOR:
                return InplaceArithmetic.IXor.create();
            case BinaryOpsConstants.INPLACE_POW:
                return InplaceArithmetic.IPow.create();
            case BinaryOpsConstants.INPLACE_MATMUL:
                return InplaceArithmetic.IMatMul.create();
            case BinaryOpsConstants.EQ:
                return BinaryComparisonNode.EqNode.create();
            case BinaryOpsConstants.NE:
                return BinaryComparisonNode.NeNode.create();
            case BinaryOpsConstants.LT:
                return BinaryComparisonNode.LtNode.create();
            case BinaryOpsConstants.LE:
                return BinaryComparisonNode.LeNode.create();
            case BinaryOpsConstants.GT:
                return BinaryComparisonNode.GtNode.create();
            case BinaryOpsConstants.GE:
                return BinaryComparisonNode.GeNode.create();
            case BinaryOpsConstants.IS:
                return IsNode.create();
            case BinaryOpsConstants.IN:
                return ContainsNode.create();
            default:
                throw CompilerDirectives.shouldNotReachHere();
        }
    };

    /*
     * Create fake GeneratorControlData just to maintain the same generator frame layout as AST
     * interpreter. TODO remove
     */
    public static final GeneratorControlData GENERATOR_CONTROL_DATA = new GeneratorControlData(new GeneratorInfo(new GeneratorInfo.Mutable()));

    private final Signature signature;
    private final TruffleString name;
    private boolean pythonInternal;

    final int celloffset;
    final int freeoffset;
    final int stackoffset;
    final int bcioffset;
    final int selfIndex;
    final int classcellIndex;

    private final CodeUnit co;
    private final Source source;
    private SourceSection sourceSection;

    @CompilationFinal(dimensions = 1) final byte[] bytecode;
    @CompilationFinal(dimensions = 1) private final Object[] consts;
    @CompilationFinal(dimensions = 1) private final long[] longConsts;
    @CompilationFinal(dimensions = 1) private final TruffleString[] names;
    @CompilationFinal(dimensions = 1) private final TruffleString[] varnames;
    @CompilationFinal(dimensions = 1) private final TruffleString[] freevars;
    @CompilationFinal(dimensions = 1) private final TruffleString[] cellvars;
    @CompilationFinal(dimensions = 1) private final int[] cell2arg;
    @CompilationFinal(dimensions = 1) protected final Assumption[] cellEffectivelyFinalAssumptions;

    @CompilationFinal(dimensions = 1) private final int[] exceptionHandlerRanges;

    /**
     * Whether instruction at given bci can put a primitive value on stack. The number is a bitwise
     * or of possible types defined by {@link QuickeningTypes}.
     */
    private final byte[] outputCanQuicken;
    /**
     * Whether store instructions to this variable should attempt to unbox primitives. The number
     * determines the type like above.
     */
    private final byte[] variableShouldUnbox;
    /**
     * Which instruction bci's have to be generalized when generalizing inputs of instruction at
     * given bci.
     */
    private final int[][] generalizeInputsMap;
    /**
     * Which store instruction bci's have to be generalized when generalizing variable with given
     * index.
     */
    private final int[][] generalizeVarsMap;
    /**
     * Current primitive types of variables. The value is one of {@link QuickeningTypes}. Used by
     * argument copying and store instructions.
     */
    @CompilationFinal(dimensions = 1) private byte[] variableTypes;

    @Children private final Node[] adoptedNodes;
    @Child private CalleeContext calleeContext = CalleeContext.create();
    @Child private PythonObjectFactory factory = PythonObjectFactory.create();
    @Child private ExceptionStateNodes.GetCaughtExceptionNode getCaughtExceptionNode;

    private final LoopConditionProfile exceptionChainProfile1 = LoopConditionProfile.createCountingProfile();
    private final LoopConditionProfile exceptionChainProfile2 = LoopConditionProfile.createCountingProfile();

    @CompilationFinal private Object osrMetadata;

    @CompilationFinal private boolean usingCachedNodes;
    @CompilationFinal(dimensions = 1) private int[] conditionProfiles;

    private static FrameDescriptor makeFrameDescriptor(CodeUnit co) {
        int capacity = co.varnames.length + co.cellvars.length + co.freevars.length + co.stacksize + 1;
        FrameDescriptor.Builder newBuilder = FrameDescriptor.newBuilder(capacity);
        newBuilder.info(new FrameInfo());
        // locals
        for (int i = 0; i < co.varnames.length; i++) {
            newBuilder.addSlot(FrameSlotKind.Illegal, co.varnames[i], null);
        }
        // cells
        for (int i = 0; i < co.cellvars.length; i++) {
            newBuilder.addSlot(FrameSlotKind.Illegal, co.cellvars[i], null);
        }
        // freevars
        for (int i = 0; i < co.freevars.length; i++) {
            newBuilder.addSlot(FrameSlotKind.Illegal, co.freevars[i], null);
        }
        // stack
        newBuilder.addSlots(co.stacksize, FrameSlotKind.Illegal);
        // BCI filled when unwinding the stack or when pausing generators
        newBuilder.addSlot(FrameSlotKind.Static, null, null);
        if (co.isGeneratorOrCoroutine()) {
            // stackTop saved when pausing a generator
            newBuilder.addSlot(FrameSlotKind.Int, null, null);
            // return value of a generator
            newBuilder.addSlot(FrameSlotKind.Illegal, null, null);
        }
        return newBuilder.build();
    }

    private static Signature makeSignature(CodeUnit co) {
        int posArgCount = co.argCount + co.positionalOnlyArgCount;
        TruffleString[] parameterNames = Arrays.copyOf(co.varnames, posArgCount);
        TruffleString[] kwOnlyNames = Arrays.copyOfRange(co.varnames, posArgCount, posArgCount + co.kwOnlyArgCount);
        int varArgsIndex = co.takesVarArgs() ? posArgCount : -1;
        return new Signature(co.positionalOnlyArgCount,
                        co.takesVarKeywordArgs(),
                        varArgsIndex,
                        co.positionalOnlyArgCount > 0,
                        parameterNames,
                        kwOnlyNames);
    }

    @TruffleBoundary
    public PBytecodeRootNode(TruffleLanguage<?> language, CodeUnit co, Source source) {
        this(language, makeFrameDescriptor(co), makeSignature(co), co, source);
    }

    @TruffleBoundary
    public PBytecodeRootNode(TruffleLanguage<?> language, FrameDescriptor fd, Signature sign, CodeUnit co, Source source) {
        super(language, fd);
        ((FrameInfo) fd.getInfo()).rootNode = this;
        this.celloffset = co.varnames.length;
        this.freeoffset = celloffset + co.cellvars.length;
        this.stackoffset = freeoffset + co.freevars.length;
        this.bcioffset = stackoffset + co.stacksize;
        this.source = source;
        this.signature = sign;
        this.bytecode = PythonUtils.arrayCopyOf(co.code, co.code.length);
        this.adoptedNodes = new Node[co.code.length];
        this.conditionProfiles = new int[co.conditionProfileCount];
        this.outputCanQuicken = co.outputCanQuicken;
        this.variableShouldUnbox = co.variableShouldUnbox;
        this.generalizeInputsMap = co.generalizeInputsMap;
        this.generalizeVarsMap = co.generalizeVarsMap;
        this.consts = co.constants;
        this.longConsts = co.primitiveConstants;
        this.names = co.names;
        this.varnames = co.varnames;
        this.freevars = co.freevars;
        this.cellvars = co.cellvars;
        this.cell2arg = co.cell2arg;
        this.name = co.name;
        this.exceptionHandlerRanges = co.exceptionHandlerRanges;
        this.co = co;
        assert co.stacksize < Math.pow(2, 12) : "stacksize cannot be larger than 12-bit range";
        cellEffectivelyFinalAssumptions = new Assumption[cellvars.length];
        for (int i = 0; i < cellvars.length; i++) {
            cellEffectivelyFinalAssumptions[i] = Truffle.getRuntime().createAssumption("cell is effectively final");
        }
        int classcellIndex = -1;
        for (int i = 0; i < this.freevars.length; i++) {
            if (T___CLASS__.equalsUncached(this.freevars[i], TS_ENCODING)) {
                classcellIndex = this.freeoffset + i;
                break;
            }
        }
        this.classcellIndex = classcellIndex;
        int selfIndex = -1;
        if (!signature.takesNoArguments()) {
            selfIndex = 0;
            if (co.cell2arg != null) {
                for (int i = 0; i < co.cell2arg.length; i++) {
                    if (co.cell2arg[i] == 0) {
                        selfIndex = celloffset + i;
                        break;
                    }
                }
            }
        }
        this.selfIndex = selfIndex;
    }

    @Override
    public String getName() {
        return name.toJavaStringUncached();
    }

    @Override
    public String toString() {
        return "<bytecode " + name + " at " + Integer.toHexString(hashCode()) + ">";
    }

    @Override
    public Signature getSignature() {
        return signature;
    }

    @Override
    public boolean isPythonInternal() {
        return pythonInternal;
    }

    public void setPythonInternal(boolean pythonInternal) {
        this.pythonInternal = pythonInternal;
    }

    public CodeUnit getCodeUnit() {
        return co;
    }

    @FunctionalInterface
    private interface NodeSupplier<T> {

        T get();
    }

    @FunctionalInterface
    private interface NodeFunction<A, T> {
        T apply(A argument);
    }

    @FunctionalInterface
    private interface IntNodeFunction<T extends Node> {
        T apply(int argument);
    }

    @SuppressWarnings("unchecked")
    private <A, T extends Node> T insertChildNode(Node[] nodes, int nodeIndex, Class<? extends T> cachedClass, NodeFunction<A, T> nodeSupplier, A argument) {
        Node node = nodes[nodeIndex];
        if (node != null && node.getClass() == cachedClass) {
            return CompilerDirectives.castExact(node, cachedClass);
        }
        return CompilerDirectives.castExact(doInsertChildNode(nodes, nodeIndex, nodeSupplier, argument), cachedClass);
    }

    @SuppressWarnings("unchecked")
    private <A, T extends Node> T doInsertChildNode(Node[] nodes, int nodeIndex, NodeFunction<A, T> nodeSupplier, A argument) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        T newNode = nodeSupplier.apply(argument);
        nodes[nodeIndex] = insert(newNode);
        return newNode;
    }

    @SuppressWarnings("unchecked")
    private <A, T extends Node> T insertChildNode(Node[] nodes, int nodeIndex, T uncached, Class<? extends T> cachedClass, NodeFunction<A, T> nodeSupplier, A argument, boolean useCachedNodes) {
        if (!useCachedNodes) {
            return uncached;
        }
        Node node = nodes[nodeIndex];
        if (node != null && node.getClass() == cachedClass) {
            return CompilerDirectives.castExact(node, cachedClass);
        }
        return CompilerDirectives.castExact(doInsertChildNode(nodes, nodeIndex, nodeSupplier, argument), cachedClass);
    }

    @SuppressWarnings("unchecked")
    private <T extends Node, U> T insertChildNodeInt(Node[] nodes, int nodeIndex, Class<U> expectedClass, IntNodeFunction<T> nodeSupplier, int argument) {
        Node node = nodes[nodeIndex];
        if (expectedClass.isInstance(node)) {
            return (T) node;
        }
        return doInsertChildNodeInt(nodes, nodeIndex, nodeSupplier, argument);
    }

    @SuppressWarnings("unchecked")
    private <T extends Node> T doInsertChildNodeInt(Node[] nodes, int nodeIndex, IntNodeFunction<T> nodeSupplier, int argument) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        T newNode = nodeSupplier.apply(argument);
        nodes[nodeIndex] = insert(newNode);
        return newNode;
    }

    @SuppressWarnings("unchecked")
    private <T extends Node, U extends T> U insertChildNode(Node[] nodes, int nodeIndex, Class<U> cachedClass, NodeSupplier<T> nodeSupplier) {
        Node node = nodes[nodeIndex];
        if (node != null && node.getClass() == cachedClass) {
            return CompilerDirectives.castExact(node, cachedClass);
        }
        return CompilerDirectives.castExact(doInsertChildNode(nodes, nodeIndex, nodeSupplier), cachedClass);
    }

    @SuppressWarnings("unchecked")
    private <T extends Node> T doInsertChildNode(Node[] nodes, int nodeIndex, NodeSupplier<T> nodeSupplier) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        T newNode = nodeSupplier.get();
        nodes[nodeIndex] = insert(newNode);
        return newNode;
    }

    @SuppressWarnings("unchecked")
    private <T extends Node> T insertChildNode(Node[] nodes, int nodeIndex, T uncached, Class<? extends T> cachedClass, NodeSupplier<T> nodeSupplier, boolean useCachedNodes) {
        if (!useCachedNodes) {
            return uncached;
        }
        Node node = nodes[nodeIndex];
        if (node != null && node.getClass() == cachedClass) {
            return CompilerDirectives.castExact(node, cachedClass);
        }
        return CompilerDirectives.castExact(doInsertChildNode(nodes, nodeIndex, nodeSupplier), cachedClass);
    }

    private static final int CONDITION_PROFILE_MAX_VALUE = 0x3fffffff;

    // Inlined from ConditionProfile.Counting#profile
    private boolean profileCondition(boolean value, byte[] localBC, int bci, boolean useCachedNodes) {
        if (!useCachedNodes) {
            return value;
        }
        int index = Byte.toUnsignedInt(localBC[bci + 2]) & Byte.toUnsignedInt(localBC[bci + 3]) << 8;
        int t = conditionProfiles[index];
        int f = conditionProfiles[index + 1];
        boolean val = value;
        if (val) {
            if (t == 0) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
            }
            if (f == 0) {
                // Make this branch fold during PE
                val = true;
            }
            if (CompilerDirectives.inInterpreter()) {
                if (t < CONDITION_PROFILE_MAX_VALUE) {
                    conditionProfiles[index] = t + 1;
                }
            }
        } else {
            if (f == 0) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
            }
            if (t == 0) {
                // Make this branch fold during PE
                val = false;
            }
            if (CompilerDirectives.inInterpreter()) {
                if (f < CONDITION_PROFILE_MAX_VALUE) {
                    conditionProfiles[index + 1] = f + 1;
                }
            }
        }
        if (CompilerDirectives.inInterpreter()) {
            // no branch probability calculation in the interpreter
            return val;
        } else {
            int sum = t + f;
            return CompilerDirectives.injectBranchProbability((double) t / (double) sum, val);
        }
    }

    @Override
    public Object getOSRMetadata() {
        return osrMetadata;
    }

    @Override
    public void setOSRMetadata(Object osrMetadata) {
        this.osrMetadata = osrMetadata;
    }

    @ExplodeLoop
    private void copyArgs(Object[] args, Frame localFrame) {
        if (variableTypes == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            copyArgsFirstTime(args, localFrame);
            return;
        }
        int argCount = co.getTotalArgCount();
        for (int i = 0; i < argCount; i++) {
            Object arg = args[i + PArguments.USER_ARGUMENTS_OFFSET];
            if (variableTypes[i] == QuickeningTypes.OBJECT) {
                localFrame.setObject(i, arg);
                continue;
            } else if (variableTypes[i] == QuickeningTypes.INT) {
                if (arg instanceof Integer) {
                    localFrame.setInt(i, (int) arg);
                    continue;
                }
            }
            // TODO other types
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeVariableStores(i);
            variableTypes[i] = QuickeningTypes.OBJECT;
            localFrame.setObject(i, arg);
        }
    }

    private void copyArgsFirstTime(Object[] args, Frame localFrame) {
        CompilerAsserts.neverPartOfCompilation();
        variableTypes = new byte[varnames.length];
        int argCount = co.getTotalArgCount();
        for (int i = 0; i < argCount; i++) {
            Object arg = args[i + PArguments.USER_ARGUMENTS_OFFSET];
            if ((variableShouldUnbox[i] & QuickeningTypes.INT) != 0 && arg instanceof Integer) {
                variableTypes[i] = QuickeningTypes.INT;
                localFrame.setInt(i, (int) arg);
            } else if ((variableShouldUnbox[i] & QuickeningTypes.BOOLEAN) != 0 && arg instanceof Boolean) {
                variableTypes[i] = QuickeningTypes.BOOLEAN;
                localFrame.setBoolean(i, (boolean) arg);
            } else {
                variableTypes[i] = QuickeningTypes.OBJECT;
                localFrame.setObject(i, arg);
            }
        }
    }

    public void createGeneratorFrame(Object[] arguments) {
        Object[] generatorFrameArguments = PArguments.create();
        MaterializedFrame generatorFrame = Truffle.getRuntime().createMaterializedFrame(generatorFrameArguments, getFrameDescriptor());
        PArguments.setGeneratorFrame(arguments, generatorFrame);
        PArguments.setCurrentFrameInfo(generatorFrameArguments, new PFrame.Reference(null));
        // The invoking node will set these two to the correct value only when the callee requests
        // it, otherwise they stay at the initial value, which we must set to null here
        PArguments.setException(arguments, null);
        PArguments.setCallerFrameInfo(arguments, null);
        PArguments.setControlData(arguments, GENERATOR_CONTROL_DATA);
        PArguments.setGeneratorFrameLocals(generatorFrameArguments, factory.createDictLocals(generatorFrame));
        copyArgsAndCells(generatorFrame, arguments);
    }

    private void copyArgsAndCells(Frame localFrame, Object[] arguments) {
        copyArgs(arguments, localFrame);
        int varIdx = co.getTotalArgCount();
        if (co.takesVarArgs()) {
            localFrame.setObject(varIdx++, factory.createTuple(PArguments.getVariableArguments(arguments)));
        }
        if (co.takesVarKeywordArgs()) {
            localFrame.setObject(varIdx, factory.createDict(PArguments.getKeywordArguments(arguments)));
        }
        initCellVars(localFrame);
        initFreeVars(localFrame, arguments);
    }

    int getInitialStackTop() {
        return stackoffset - 1;
    }

    @Override
    public Object execute(VirtualFrame virtualFrame) {
        calleeContext.enter(virtualFrame);
        try {
            if (!co.isGeneratorOrCoroutine()) {
                copyArgsAndCells(virtualFrame, virtualFrame.getArguments());
            }

            return executeFromBci(virtualFrame, virtualFrame, this, 0, getInitialStackTop(), Integer.MAX_VALUE);
        } finally {
            calleeContext.exit(virtualFrame, this);
        }
    }

    @Override
    public Object[] storeParentFrameInArguments(VirtualFrame parentFrame) {
        Object[] arguments = parentFrame.getArguments();
        PArguments.setOSRFrame(arguments, parentFrame);
        return arguments;
    }

    @Override
    public Frame restoreParentFrameFromArguments(Object[] arguments) {
        return PArguments.getOSRFrame(arguments);
    }

    @Override
    public Object executeOSR(VirtualFrame osrFrame, int target, Object interpreterStateObject) {
        OSRInterpreterState interpreterState = (OSRInterpreterState) interpreterStateObject;
        return executeFromBci(osrFrame, osrFrame, this, target, interpreterState.stackTop, interpreterState.loopEndBci);
    }

    private static final class OSRContinuation {
        public final int bci;
        public final int stackTop;

        private OSRContinuation(int bci, int stackTop) {
            this.bci = bci;
            this.stackTop = stackTop;
        }
    }

    @ValueType
    private static final class MutableLoopData {
        int loopCount;
        /*
         * This separate tracking of local exception is necessary to make exception state saving
         * work in generators. On one hand we need to retain the exception that was caught in the
         * generator, on the other hand we don't want to retain the exception state that was passed
         * from the outer frame because that changes with every resume.
         */
        boolean fetchedException;
        PException outerException;
        PException localException;
    }

    Object executeFromBci(VirtualFrame virtualFrame, Frame localFrame, BytecodeOSRNode osrNode, int initialBci, int initialStackTop, int loopEndBci) {
        /*
         * A lot of python code is executed just a single time, such as top level module code. We
         * want to save some time and memory by trying to first use uncached nodes. We use two
         * separate entry points so that they get each get compiled with monomorphic calls to either
         * cached or uncached nodes.
         */
        if (usingCachedNodes) {
            return executeCached(virtualFrame, localFrame, osrNode, initialBci, initialStackTop, loopEndBci);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            usingCachedNodes = true;
            Object result = executeUncached(virtualFrame, localFrame, osrNode, initialBci, initialStackTop, loopEndBci);
            if (result instanceof OSRContinuation) {
                OSRContinuation continuation = (OSRContinuation) result;
                return executeCached(virtualFrame, localFrame, osrNode, continuation.bci, continuation.stackTop, loopEndBci);
            }
            return result;
        }
    }

    @BytecodeInterpreterSwitch
    private Object executeCached(VirtualFrame virtualFrame, Frame localFrame, BytecodeOSRNode osrNode, int initialBci, int initialStackTop, int loopEndBci) {
        return bytecodeLoop(virtualFrame, localFrame, osrNode, initialBci, initialStackTop, loopEndBci, true);
    }

    @BytecodeInterpreterSwitch
    private Object executeUncached(VirtualFrame virtualFrame, Frame localFrame, BytecodeOSRNode osrNode, int initialBci, int initialStackTop, int loopEndBci) {
        return bytecodeLoop(virtualFrame, localFrame, osrNode, initialBci, initialStackTop, loopEndBci, false);
    }

    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.MERGE_EXPLODE)
    @SuppressWarnings("fallthrough")
    @BytecodeInterpreterSwitch
    private Object bytecodeLoop(VirtualFrame virtualFrame, Frame localFrame, BytecodeOSRNode osrNode, int initialBci, int initialStackTop, int loopEndBci, boolean useCachedNodes) {
        boolean wasCompiled = CompilerDirectives.inCompiledCode();
        Object[] arguments = virtualFrame.getArguments();
        Object globals = PArguments.getGlobals(arguments);
        Object locals = PArguments.getSpecialArgument(arguments);

        /*
         * We use an object as a workaround for not being able to specify which local variables are
         * loop constants (GR-35338).
         */
        MutableLoopData mutableData = new MutableLoopData();
        int stackTop = initialStackTop;
        int bci = initialBci;

        boolean isGeneratorOrCoroutine = co.isGeneratorOrCoroutine();
        byte[] localBC = bytecode;
        Object[] localConsts = consts;
        long[] localLongConsts = longConsts;
        TruffleString[] localNames = names;
        Node[] localNodes = adoptedNodes;
        final int bciSlot = bcioffset;
        final int localCelloffset = celloffset;

        setCurrentBci(virtualFrame, bciSlot, initialBci);

        CompilerAsserts.partialEvaluationConstant(localBC);
        CompilerAsserts.partialEvaluationConstant(bci);
        CompilerAsserts.partialEvaluationConstant(stackTop);

        int oparg = 0;
        while (true) {
            final byte bc = localBC[bci];
            final int beginBci = bci;

            CompilerAsserts.partialEvaluationConstant(bc);
            CompilerAsserts.partialEvaluationConstant(bci);
            CompilerAsserts.partialEvaluationConstant(stackTop);

            if (wasCompiled && bci > loopEndBci) {
                /*
                 * This means we're in OSR and we just jumped out of the OSR compiled loop. We want
                 * to return to the caller to continue in interpreter again otherwise we would most
                 * likely deopt on the next instruction. The caller handles the special return value
                 * in JUMP_BACKWARD. In generators, we need to additionally copy the stack items
                 * back to the generator frame.
                 */
                if (localFrame != virtualFrame) {
                    copyStackSlotsToGeneratorFrame(virtualFrame, localFrame, stackTop);
                    // Clear slots that were popped (if any)
                    clearFrameSlots(localFrame, stackTop + 1, initialStackTop);
                }
                return new OSRContinuation(bci, stackTop);
            }

            try {
                switch (bc) {
                    case OpCodesConstants.LOAD_NONE:
                        virtualFrame.setObject(++stackTop, PNone.NONE);
                        break;
                    case OpCodesConstants.LOAD_ELLIPSIS:
                        virtualFrame.setObject(++stackTop, PEllipsis.INSTANCE);
                        break;
                    case OpCodesConstants.LOAD_TRUE_B:
                        virtualFrame.setBoolean(++stackTop, true);
                        break;
                    case OpCodesConstants.LOAD_TRUE_O:
                        virtualFrame.setObject(++stackTop, true);
                        break;
                    case OpCodesConstants.LOAD_FALSE_B:
                        virtualFrame.setBoolean(++stackTop, false);
                        break;
                    case OpCodesConstants.LOAD_FALSE_O:
                        virtualFrame.setObject(++stackTop, false);
                        break;
                    case OpCodesConstants.LOAD_BYTE_I:
                        virtualFrame.setInt(++stackTop, localBC[++bci]); // signed!
                        break;
                    case OpCodesConstants.LOAD_BYTE_O:
                        virtualFrame.setObject(++stackTop, (int) localBC[++bci]); // signed!
                        break;
                    case OpCodesConstants.LOAD_INT_I: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        virtualFrame.setInt(++stackTop, (int) localLongConsts[oparg]);
                        break;
                    }
                    case OpCodesConstants.LOAD_INT_O: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        virtualFrame.setObject(++stackTop, (int) localLongConsts[oparg]);
                        break;
                    }
                    case OpCodesConstants.LOAD_LONG: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        virtualFrame.setObject(++stackTop, localLongConsts[oparg]);
                        break;
                    }
                    case OpCodesConstants.LOAD_DOUBLE: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        virtualFrame.setObject(++stackTop, Double.longBitsToDouble(localLongConsts[oparg]));
                        break;
                    }
                    case OpCodesConstants.LOAD_BIGINT: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        virtualFrame.setObject(++stackTop, factory.createInt((BigInteger) localConsts[oparg]));
                        break;
                    }
                    case OpCodesConstants.LOAD_STRING:
                    case OpCodesConstants.LOAD_CONST: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        virtualFrame.setObject(++stackTop, localConsts[oparg]);
                        break;
                    }
                    case OpCodesConstants.LOAD_BYTES: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        virtualFrame.setObject(++stackTop, factory.createBytes((byte[]) localConsts[oparg]));
                        break;
                    }
                    case OpCodesConstants.LOAD_COMPLEX: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        double[] num = (double[]) localConsts[oparg];
                        virtualFrame.setObject(++stackTop, factory.createComplex(num[0], num[1]));
                        break;
                    }
                    case OpCodesConstants.MAKE_KEYWORD: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        TruffleString key = (TruffleString) localConsts[oparg];
                        Object value = virtualFrame.getObject(stackTop);
                        virtualFrame.setObject(stackTop, new PKeyword(key, value));
                        break;
                    }
                    case OpCodesConstants.BUILD_SLICE: {
                        int count = Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeBuildSlice(virtualFrame, stackTop, beginBci, count, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.FORMAT_VALUE: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        int options = Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeFormatValue(virtualFrame, stackTop, beginBci, localNodes, options, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.COLLECTION_FROM_COLLECTION: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        int type = Byte.toUnsignedInt(localBC[++bci]);
                        bytecodeCollectionFromCollection(virtualFrame, type, stackTop, localNodes, beginBci, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.COLLECTION_ADD_COLLECTION: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        /*
                         * The first collection must be in the target format already, the second one
                         * is a python object.
                         */
                        int type = Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeCollectionAddCollection(virtualFrame, type, stackTop, localNodes, beginBci, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.COLLECTION_FROM_STACK: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        int countAndType = Byte.toUnsignedInt(localBC[++bci]);
                        int count = CollectionBits.elementCount(countAndType);
                        int type = CollectionBits.elementType(countAndType);
                        stackTop = bytecodeCollectionFromStack(virtualFrame, type, count, stackTop, localNodes, beginBci, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.COLLECTION_ADD_STACK: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        int countAndType = Byte.toUnsignedInt(localBC[++bci]);
                        int count = CollectionBits.elementCount(countAndType);
                        int type = CollectionBits.elementType(countAndType);
                        // Just combine COLLECTION_FROM_STACK and COLLECTION_ADD_COLLECTION for now
                        stackTop = bytecodeCollectionFromStack(virtualFrame, type, count, stackTop, localNodes, beginBci, useCachedNodes);
                        stackTop = bytecodeCollectionAddCollection(virtualFrame, type, stackTop, localNodes, beginBci + 1, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.ADD_TO_COLLECTION: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        int depthAndType = Byte.toUnsignedInt(localBC[++bci]);
                        int depth = CollectionBits.elementCount(depthAndType);
                        int type = CollectionBits.elementType(depthAndType);
                        stackTop = bytecodeAddToCollection(virtualFrame, stackTop, beginBci, localNodes, depth, type, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.KWARGS_DICT_MERGE: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        KwargsMergeNode mergeNode = insertChildNode(localNodes, bci, UNCACHED_KWARGS_MERGE, KwargsMergeNodeGen.class, NODE_KWARGS_MERGE, useCachedNodes);
                        stackTop = mergeNode.execute(virtualFrame, stackTop);
                        break;
                    }
                    case OpCodesConstants.UNPACK_SEQUENCE: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeUnpackSequence(virtualFrame, stackTop, beginBci, localNodes, oparg, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.UNPACK_EX: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        int countAfter = Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeUnpackEx(virtualFrame, stackTop, beginBci, localNodes, oparg, countAfter, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.NOP:
                        break;
                    case OpCodesConstants.LOAD_FAST: {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeLoadFastAdaptive(virtualFrame, localFrame, ++stackTop, localBC, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.LOAD_FAST_O: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeLoadFastO(virtualFrame, localFrame, ++stackTop, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.LOAD_FAST_I: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeLoadFastI(virtualFrame, localFrame, ++stackTop, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.LOAD_FAST_I_BOX: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeLoadFastIBox(virtualFrame, localFrame, ++stackTop, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.LOAD_FAST_B: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeLoadFastB(virtualFrame, localFrame, ++stackTop, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.LOAD_FAST_B_BOX: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeLoadFastBBox(virtualFrame, localFrame, ++stackTop, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.LOAD_CLOSURE: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        PCell cell = (PCell) localFrame.getObject(localCelloffset + oparg);
                        virtualFrame.setObject(++stackTop, cell);
                        break;
                    }
                    case OpCodesConstants.CLOSURE_FROM_STACK: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeClosureFromStack(virtualFrame, stackTop, oparg);
                        break;
                    }
                    case OpCodesConstants.LOAD_CLASSDEREF: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeLoadClassDeref(virtualFrame, localFrame, locals, stackTop, beginBci, localNodes, oparg, localCelloffset, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.LOAD_DEREF: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeLoadDeref(virtualFrame, localFrame, stackTop, beginBci, localNodes, oparg, localCelloffset, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.STORE_DEREF: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeStoreDeref(virtualFrame, localFrame, stackTop, oparg, localCelloffset);
                        break;
                    }
                    case OpCodesConstants.DELETE_DEREF: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        bytecodeDeleteDeref(localFrame, beginBci, localNodes, oparg, localCelloffset, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.STORE_FAST: {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeStoreFastAdaptive(virtualFrame, localFrame, stackTop--, bci++, localBC, oparg);
                        break;
                    }
                    case OpCodesConstants.STORE_FAST_O: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeStoreFastO(virtualFrame, localFrame, stackTop--, oparg);
                        bci++;
                        break;
                    }
                    case OpCodesConstants.STORE_FAST_UNBOX_I: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeStoreFastUnboxI(virtualFrame, localFrame, stackTop--, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.STORE_FAST_I: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeStoreFastI(virtualFrame, localFrame, stackTop--, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.STORE_FAST_UNBOX_B: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeStoreFastUnboxB(virtualFrame, localFrame, stackTop--, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.STORE_FAST_B: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeStoreFastB(virtualFrame, localFrame, stackTop--, bci++, oparg);
                        break;
                    }
                    case OpCodesConstants.POP_TOP:
                        virtualFrame.setObject(stackTop--, null);
                        break;
                    case OpCodesConstants.ROT_TWO: {
                        Object top = virtualFrame.getObject(stackTop);
                        virtualFrame.setObject(stackTop, virtualFrame.getObject(stackTop - 1));
                        virtualFrame.setObject(stackTop - 1, top);
                        break;
                    }
                    case OpCodesConstants.ROT_THREE: {
                        Object top = virtualFrame.getObject(stackTop);
                        virtualFrame.setObject(stackTop, virtualFrame.getObject(stackTop - 1));
                        virtualFrame.setObject(stackTop - 1, virtualFrame.getObject(stackTop - 2));
                        virtualFrame.setObject(stackTop - 2, top);
                        break;
                    }
                    case OpCodesConstants.DUP_TOP:
                        virtualFrame.setObject(stackTop + 1, virtualFrame.getObject(stackTop));
                        stackTop++;
                        break;
                    case OpCodesConstants.UNARY_OP: {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        bytecodeUnaryOpAdaptive(virtualFrame, stackTop, bci++, localBC, localNodes);
                        break;
                    }
                    case OpCodesConstants.UNARY_OP_O_O: {
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeUnaryOpOO(virtualFrame, stackTop, bci++, localNodes, op, bciSlot);
                        break;
                    }
                    case OpCodesConstants.UNARY_OP_I_I: {
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeUnaryOpII(virtualFrame, stackTop, bci++, op);
                        break;
                    }
                    case OpCodesConstants.UNARY_OP_I_O: {
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeUnaryOpIO(virtualFrame, stackTop, bci++, op);
                        break;
                    }
                    case OpCodesConstants.UNARY_OP_B_B: {
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeUnaryOpBB(virtualFrame, stackTop, bci++, op);
                        break;
                    }
                    case OpCodesConstants.UNARY_OP_B_O: {
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeUnaryOpBO(virtualFrame, stackTop, bci++, op);
                        break;
                    }
                    case OpCodesConstants.BINARY_OP: {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeBinaryOpAdaptive(virtualFrame, stackTop--, localBC, bci++, localNodes, op, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.BINARY_OP_OO_O: {
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeBinaryOpOOO(virtualFrame, stackTop--, bci++, localNodes, op, bciSlot);
                        break;
                    }
                    case OpCodesConstants.BINARY_OP_II_I: {
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeBinaryOpIII(virtualFrame, stackTop--, bci++, localNodes, op, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.BINARY_OP_II_B: {
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeBinaryOpIIB(virtualFrame, stackTop--, bci++, op);
                        break;
                    }
                    case OpCodesConstants.BINARY_OP_II_O: {
                        int op = Byte.toUnsignedInt(localBC[bci + 1]);
                        bytecodeBinaryOpIIO(virtualFrame, stackTop--, bci++, localNodes, op);
                        break;
                    }
                    case OpCodesConstants.BINARY_SUBSCR: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        GetItemNode getItemNode = insertChildNode(localNodes, bci, GetItemNodeGen.class, NODE_GET_ITEM);
                        Object slice = virtualFrame.getObject(stackTop);
                        virtualFrame.setObject(stackTop--, null);
                        virtualFrame.setObject(stackTop, getItemNode.execute(virtualFrame, virtualFrame.getObject(stackTop), slice));
                        break;
                    }
                    case OpCodesConstants.STORE_SUBSCR: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        stackTop = bytecodeStoreSubscr(virtualFrame, stackTop, beginBci, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.DELETE_SUBSCR: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        stackTop = bytecodeDeleteSubscr(virtualFrame, stackTop, beginBci, localNodes);
                        break;
                    }
                    case OpCodesConstants.RAISE_VARARGS: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        int count = Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeRaiseVarargs(virtualFrame, stackTop, beginBci, count, localNodes);
                        break;
                    }
                    case OpCodesConstants.RETURN_VALUE: {
                        if (CompilerDirectives.hasNextTier() && mutableData.loopCount > 0) {
                            LoopNode.reportLoopCount(this, mutableData.loopCount);
                        }
                        Object value = virtualFrame.getObject(stackTop);
                        if (isGeneratorOrCoroutine) {
                            return GeneratorResult.createReturn(value);
                        } else {
                            return value;
                        }
                    }
                    case OpCodesConstants.LOAD_BUILD_CLASS: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        ReadGlobalOrBuiltinNode read = insertChildNode(localNodes, beginBci, UNCACHED_READ_GLOBAL_OR_BUILTIN, ReadGlobalOrBuiltinNodeGen.class, NODE_READ_GLOBAL_OR_BUILTIN_BUILD_CLASS,
                                        useCachedNodes);
                        virtualFrame.setObject(++stackTop, read.read(virtualFrame, globals, T___BUILD_CLASS__));
                        break;
                    }
                    case OpCodesConstants.LOAD_ASSERTION_ERROR: {
                        virtualFrame.setObject(++stackTop, PythonBuiltinClassType.AssertionError);
                        break;
                    }
                    case OpCodesConstants.STORE_NAME: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeStoreName(virtualFrame, stackTop, beginBci, oparg, localNames, localNodes);
                        break;
                    }
                    case OpCodesConstants.DELETE_NAME: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        bytecodeDeleteName(virtualFrame, globals, locals, beginBci, oparg, localNames, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.STORE_ATTR: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeStoreAttr(virtualFrame, stackTop, beginBci, oparg, localNodes, localNames, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.DELETE_ATTR: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeDeleteAttr(virtualFrame, stackTop, beginBci, oparg, localNodes, localNames, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.STORE_GLOBAL: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeStoreGlobal(virtualFrame, globals, stackTop, beginBci, oparg, localNodes, localNames, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.DELETE_GLOBAL: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        bytecodeDeleteGlobal(virtualFrame, globals, beginBci, oparg, localNodes, localNames);
                        break;
                    }
                    case OpCodesConstants.LOAD_NAME: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeLoadName(virtualFrame, stackTop, beginBci, oparg, localNodes, localNames);
                        break;
                    }
                    case OpCodesConstants.LOAD_GLOBAL: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeLoadGlobal(virtualFrame, globals, stackTop, beginBci, localNames[oparg], localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.DELETE_FAST: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        bytecodeDeleteFast(localFrame, beginBci, localNodes, oparg, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.LOAD_ATTR: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        bytecodeLoadAttr(virtualFrame, stackTop, beginBci, oparg, localNodes, localNames, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.IMPORT_NAME: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeImportName(virtualFrame, globals, stackTop, beginBci, oparg, localNames, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.IMPORT_FROM: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeImportFrom(virtualFrame, stackTop, beginBci, oparg, localNames, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.IMPORT_STAR: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeImportStar(virtualFrame, stackTop, beginBci, oparg, localNames, localNodes);
                        break;
                    }
                    case OpCodesConstants.JUMP_FORWARD:
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bci += oparg;
                        oparg = 0;
                        continue;
                    case OpCodesConstants.POP_AND_JUMP_IF_FALSE: {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        if (virtualFrame.isBoolean(stackTop)) {
                            bytecode[bci] = OpCodesConstants.POP_AND_JUMP_IF_FALSE_B;
                        } else {
                            bytecode[bci] = OpCodesConstants.POP_AND_JUMP_IF_FALSE_O;
                        }
                        continue;
                    }
                    case OpCodesConstants.POP_AND_JUMP_IF_TRUE: {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        if (virtualFrame.isBoolean(stackTop)) {
                            bytecode[bci] = OpCodesConstants.POP_AND_JUMP_IF_TRUE_B;
                        } else {
                            bytecode[bci] = OpCodesConstants.POP_AND_JUMP_IF_TRUE_O;
                        }
                        continue;
                    }
                    case OpCodesConstants.POP_AND_JUMP_IF_FALSE_O: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        if (profileCondition(!bytecodePopCondition(virtualFrame, stackTop--, localNodes, bci, useCachedNodes), localBC, bci, useCachedNodes)) {
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        } else {
                            bci += 3;
                        }
                        break;
                    }
                    case OpCodesConstants.POP_AND_JUMP_IF_TRUE_O: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        if (profileCondition(bytecodePopCondition(virtualFrame, stackTop--, localNodes, bci, useCachedNodes), localBC, bci, useCachedNodes)) {
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        } else {
                            bci += 3;
                        }
                        break;
                    }
                    case OpCodesConstants.POP_AND_JUMP_IF_FALSE_B: {
                        if (!virtualFrame.isBoolean(stackTop)) {
                            CompilerDirectives.transferToInterpreterAndInvalidate();
                            bytecode[bci] = OpCodesConstants.POP_AND_JUMP_IF_FALSE_O;
                            continue;
                        }
                        if (profileCondition(!virtualFrame.getBoolean(stackTop--), localBC, bci, useCachedNodes)) {
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        } else {
                            bci += 3;
                        }
                        break;
                    }
                    case OpCodesConstants.POP_AND_JUMP_IF_TRUE_B: {
                        if (!virtualFrame.isBoolean(stackTop)) {
                            CompilerDirectives.transferToInterpreterAndInvalidate();
                            bytecode[bci] = OpCodesConstants.POP_AND_JUMP_IF_TRUE_O;
                            continue;
                        }
                        if (profileCondition(virtualFrame.getBoolean(stackTop--), localBC, bci, useCachedNodes)) {
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        } else {
                            bci += 3;
                        }
                        break;
                    }
                    case OpCodesConstants.JUMP_IF_FALSE_OR_POP: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        PyObjectIsTrueNode isTrue = insertChildNode(localNodes, beginBci, UNCACHED_OBJECT_IS_TRUE, PyObjectIsTrueNodeGen.class, NODE_OBJECT_IS_TRUE, useCachedNodes);
                        Object cond = virtualFrame.getObject(stackTop);
                        if (profileCondition(!isTrue.execute(virtualFrame, cond), localBC, bci, useCachedNodes)) {
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        } else {
                            virtualFrame.setObject(stackTop--, null);
                            bci += 3;
                        }
                        break;
                    }
                    case OpCodesConstants.JUMP_IF_TRUE_OR_POP: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        PyObjectIsTrueNode isTrue = insertChildNode(localNodes, beginBci, UNCACHED_OBJECT_IS_TRUE, PyObjectIsTrueNodeGen.class, NODE_OBJECT_IS_TRUE, useCachedNodes);
                        Object cond = virtualFrame.getObject(stackTop);
                        if (profileCondition(isTrue.execute(virtualFrame, cond), localBC, bci, useCachedNodes)) {
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        } else {
                            virtualFrame.setObject(stackTop--, null);
                            bci += 3;
                        }
                        break;
                    }
                    case OpCodesConstants.JUMP_BACKWARD: {
                        oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                        bci -= oparg;
                        if (CompilerDirectives.hasNextTier()) {
                            mutableData.loopCount++;
                        }
                        if (CompilerDirectives.inInterpreter()) {
                            if (!useCachedNodes) {
                                return new OSRContinuation(bci, stackTop);
                            }
                            if (BytecodeOSRNode.pollOSRBackEdge(osrNode)) {
                                /*
                                 * Beware of race conditions when adding more things to the
                                 * interpreterState argument. It gets stored already at this point,
                                 * but the compilation runs in parallel. The compiled code may get
                                 * entered from a different invocation of this root, using the
                                 * interpreterState that was saved here. Don't put any data specific
                                 * to particular invocation in there (like python-level arguments or
                                 * variables) or it will get mixed up. To retain such state, put it
                                 * into the frame instead.
                                 */
                                Object osrResult = BytecodeOSRNode.tryOSR(osrNode, bci, new OSRInterpreterState(stackTop, beginBci), null, virtualFrame);
                                if (osrResult != null) {
                                    if (osrResult instanceof OSRContinuation) {
                                        // We should continue executing in interpreter after the
                                        // loop
                                        OSRContinuation continuation = (OSRContinuation) osrResult;
                                        bci = continuation.bci;
                                        stackTop = continuation.stackTop;
                                        oparg = 0;
                                        continue;
                                    } else {
                                        // We reached a return/yield
                                        if (CompilerDirectives.hasNextTier() && mutableData.loopCount > 0) {
                                            LoopNode.reportLoopCount(this, mutableData.loopCount);
                                        }
                                        return osrResult;
                                    }
                                }
                            }
                        }
                        TruffleSafepoint.poll(this);
                        oparg = 0;
                        continue;
                    }
                    case OpCodesConstants.GET_ITER: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        PyObjectGetIter getIter = insertChildNode(localNodes, beginBci, UNCACHED_OBJECT_GET_ITER, PyObjectGetIterNodeGen.class, NODE_OBJECT_GET_ITER, useCachedNodes);
                        virtualFrame.setObject(stackTop, getIter.execute(virtualFrame, virtualFrame.getObject(stackTop)));
                        break;
                    }
                    case OpCodesConstants.FOR_ITER: {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        if ((outputCanQuicken[bci] & QuickeningTypes.INT) != 0) {
                            bytecode[bci] = OpCodesConstants.FOR_ITER_I;
                        } else {
                            bytecode[bci] = OpCodesConstants.FOR_ITER_O;
                        }
                        continue;
                    }
                    case OpCodesConstants.FOR_ITER_O: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        ForIterONode node = insertChildNode(localNodes, beginBci, UNCACHED_FOR_ITER_O, ForIterONodeGen.class, NODE_FOR_ITER_O, useCachedNodes);
                        boolean cont = node.execute(virtualFrame, virtualFrame.getObject(stackTop), stackTop + 1);
                        if (cont) {
                            stackTop++;
                            bci++;
                        } else {
                            virtualFrame.setObject(stackTop--, null);
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        }
                        break;
                    }
                    case OpCodesConstants.FOR_ITER_I: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        ForIterINode node = insertChildNode(localNodes, beginBci, UNCACHED_FOR_ITER_I, ForIterINodeGen.class, NODE_FOR_ITER_I, useCachedNodes);
                        boolean cont = true;
                        try {
                            cont = node.execute(virtualFrame, virtualFrame.getObject(stackTop), stackTop + 1);
                        } catch (QuickeningGeneralizeException e) {
                            CompilerDirectives.transferToInterpreterAndInvalidate();
                            if (e.type == QuickeningTypes.OBJECT) {
                                bytecode[bci] = OpCodesConstants.FOR_ITER_O;
                            } else {
                                throw CompilerDirectives.shouldNotReachHere("invalid type");
                            }
                        }
                        if (cont) {
                            stackTop++;
                            bci++;
                        } else {
                            virtualFrame.setObject(stackTop--, null);
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        }
                        break;
                    }
                    case OpCodesConstants.LOAD_METHOD: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeLoadMethod(virtualFrame, stackTop, bci, oparg, localNames, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.CALL_METHOD: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        int argcount = Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeCallMethod(virtualFrame, stackTop, beginBci, argcount, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.CALL_METHOD_VARARGS: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        bytecodeCallMethodVarargs(virtualFrame, stackTop, beginBci, localNames, oparg, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.CALL_FUNCTION: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        stackTop = bytecodeCallFunction(virtualFrame, stackTop, beginBci, oparg, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.CALL_FUNCTION_VARARGS: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        stackTop = bytecodeCallFunctionVarargs(virtualFrame, stackTop, beginBci, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.CALL_FUNCTION_KW: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        stackTop = bytecodeCallFunctionKw(virtualFrame, stackTop, beginBci, localNodes, useCachedNodes);
                        break;
                    }
                    case OpCodesConstants.MAKE_FUNCTION: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        int flags = Byte.toUnsignedInt(localBC[++bci]);
                        CodeUnit codeUnit = (CodeUnit) localConsts[oparg];
                        MakeFunctionNode makeFunctionNode = insertChildNode(localNodes, beginBci, MakeFunctionNodeGen.class, () -> MakeFunctionNode.create(PythonLanguage.get(this), codeUnit, source));
                        stackTop = makeFunctionNode.execute(virtualFrame, globals, stackTop, flags);
                        break;
                    }
                    case OpCodesConstants.SETUP_ANNOTATIONS: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        SetupAnnotationsNode setupAnnotationsNode = insertChildNode(localNodes, beginBci, UNCACHED_SETUP_ANNOTATIONS, SetupAnnotationsNodeGen.class, NODE_SETUP_ANNOTATIONS,
                                        useCachedNodes);
                        setupAnnotationsNode.execute(virtualFrame);
                        break;
                    }
                    case OpCodesConstants.MATCH_EXC_OR_JUMP: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        Object exception = virtualFrame.getObject(stackTop - 1);
                        Object matchType = virtualFrame.getObject(stackTop);
                        virtualFrame.setObject(stackTop--, null);
                        ExceptMatchNode matchNode = insertChildNode(localNodes, beginBci, UNCACHED_EXCEPT_MATCH, ExceptMatchNodeGen.class, NODE_EXCEPT_MATCH, useCachedNodes);
                        boolean match = false;
                        if (!(exception instanceof PException) && matchType == null) {
                            match = true;
                        }
                        if (!match) {
                            match = matchNode.executeMatch(virtualFrame, exception, matchType);
                        }
                        if (profileCondition(!match, localBC, bci, useCachedNodes)) {
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        } else {
                            bci += 3;
                        }
                        break;
                    }
                    case OpCodesConstants.UNWRAP_EXC: {
                        Object exception = virtualFrame.getObject(stackTop);
                        if (exception instanceof PException) {
                            virtualFrame.setObject(stackTop, ((PException) exception).getEscapedException());
                        }
                        // Let interop exceptions be
                        break;
                    }
                    case OpCodesConstants.SETUP_WITH: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        SetupWithNode setupWithNode = insertChildNode(localNodes, beginBci, UNCACHED_SETUP_WITH_NODE, SetupWithNodeGen.class, NODE_SETUP_WITH, useCachedNodes);
                        stackTop = setupWithNode.execute(virtualFrame, stackTop);
                        break;
                    }
                    case OpCodesConstants.EXIT_WITH: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        ExitWithNode exitWithNode = insertChildNode(localNodes, beginBci, UNCACHED_EXIT_WITH_NODE, ExitWithNodeGen.class, NODE_EXIT_WITH, useCachedNodes);
                        stackTop = exitWithNode.execute(virtualFrame, stackTop);
                        break;
                    }
                    case OpCodesConstants.PUSH_EXC_INFO: {
                        Object exception = virtualFrame.getObject(stackTop);
                        Object origException = exception;
                        if (!(exception instanceof PException)) {
                            exception = wrapJavaException((Throwable) exception, factory.createBaseException(PythonErrorType.SystemError, ErrorMessages.M, new Object[]{exception}));
                        }
                        if (!mutableData.fetchedException) {
                            mutableData.outerException = PArguments.getException(arguments);
                            mutableData.fetchedException = true;
                        }
                        virtualFrame.setObject(stackTop++, mutableData.localException);
                        mutableData.localException = (PException) exception;
                        PArguments.setException(arguments, mutableData.localException);
                        virtualFrame.setObject(stackTop, origException);
                        break;
                    }
                    case OpCodesConstants.POP_EXCEPT: {
                        mutableData.localException = popExceptionState(arguments, virtualFrame.getObject(stackTop), mutableData.outerException);
                        virtualFrame.setObject(stackTop--, null);
                        break;
                    }
                    case OpCodesConstants.END_EXC_HANDLER: {
                        mutableData.localException = popExceptionState(arguments, virtualFrame.getObject(stackTop - 1), mutableData.outerException);
                        throw bytecodeEndExcHandler(virtualFrame, stackTop);
                    }
                    case OpCodesConstants.YIELD_VALUE: {
                        if (CompilerDirectives.hasNextTier() && mutableData.loopCount > 0) {
                            LoopNode.reportLoopCount(this, mutableData.loopCount);
                        }
                        Object value = virtualFrame.getObject(stackTop);
                        virtualFrame.setObject(stackTop--, null);
                        PArguments.setException(PArguments.getGeneratorFrame(arguments), mutableData.localException);
                        // See PBytecodeGeneratorRootNode#execute
                        if (localFrame != virtualFrame) {
                            copyStackSlotsToGeneratorFrame(virtualFrame, localFrame, stackTop);
                            // Clear slots that were popped (if any)
                            clearFrameSlots(localFrame, stackTop + 1, initialStackTop);
                        }
                        return GeneratorResult.createYield(bci + 1, stackTop, value);
                    }
                    case OpCodesConstants.RESUME_YIELD: {
                        mutableData.localException = PArguments.getException(PArguments.getGeneratorFrame(arguments));
                        if (mutableData.localException != null) {
                            PArguments.setException(arguments, mutableData.localException);
                        }
                        Object sendValue = PArguments.getSpecialArgument(arguments);
                        if (sendValue == null) {
                            sendValue = PNone.NONE;
                        } else if (sendValue instanceof ThrowData) {
                            ThrowData throwData = (ThrowData) sendValue;
                            throw PException.fromObject(throwData.pythonException, this, throwData.withJavaStacktrace);
                        }
                        virtualFrame.setObject(++stackTop, sendValue);
                        break;
                    }
                    case OpCodesConstants.SEND: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        Object value = virtualFrame.getObject(stackTop);
                        Object obj = virtualFrame.getObject(stackTop - 1);
                        SendNode sendNode = insertChildNode(localNodes, beginBci, SendNodeGen.class, NODE_SEND);
                        boolean returned = sendNode.execute(virtualFrame, stackTop, obj, value);
                        if (!returned) {
                            bci++;
                            break;
                        } else {
                            stackTop--;
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        }
                    }
                    case OpCodesConstants.THROW: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        Object exception = virtualFrame.getObject(stackTop);
                        if (!(exception instanceof PException)) {
                            throw CompilerDirectives.shouldNotReachHere("interop exceptions not supported in throw");
                        }
                        Object obj = virtualFrame.getObject(stackTop - 1);
                        ThrowNode throwNode = insertChildNode(localNodes, beginBci, ThrowNodeGen.class, NODE_THROW);
                        boolean returned = throwNode.execute(virtualFrame, stackTop, obj, (PException) exception);
                        if (!returned) {
                            bci++;
                            break;
                        } else {
                            stackTop--;
                            oparg |= Byte.toUnsignedInt(localBC[bci + 1]);
                            bci += oparg;
                            oparg = 0;
                            continue;
                        }
                    }
                    case OpCodesConstants.PRINT_EXPR: {
                        setCurrentBci(virtualFrame, bciSlot, bci);
                        PrintExprNode printExprNode = insertChildNode(localNodes, beginBci, UNCACHED_PRINT_EXPR, PrintExprNodeGen.class, NODE_PRINT_EXPR, useCachedNodes);
                        printExprNode.execute(virtualFrame, virtualFrame.getObject(stackTop));
                        virtualFrame.setObject(stackTop--, null);
                        break;
                    }
                    case OpCodesConstants.EXTENDED_ARG: {
                        oparg |= Byte.toUnsignedInt(localBC[++bci]);
                        oparg <<= 8;
                        bci++;
                        continue;
                    }
                    default:
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        throw PRaiseNode.raiseUncached(this, SystemError, toTruffleStringUncached("not implemented bytecode %s"), OpCodes.VALUES[bc]);
                }
                // prepare next loop
                oparg = 0;
                bci++;
            } catch (PythonExitException e) {
                throw e;
            } catch (Exception | StackOverflowError | AssertionError e) {
                PException pe = null;
                boolean isInteropException = false;
                if (e instanceof PException) {
                    pe = (PException) e;
                } else if (e instanceof AbstractTruffleException) {
                    isInteropException = true;
                } else {
                    pe = wrapJavaExceptionIfApplicable(e);
                    if (pe == null) {
                        throw e;
                    }
                }

                int targetIndex = findHandler(bci);
                CompilerAsserts.partialEvaluationConstant(targetIndex);
                if (pe != null) {
                    if (mutableData.localException != null) {
                        ExceptionHandlingStatementNode.chainExceptions(pe.getUnreifiedException(), mutableData.localException, exceptionChainProfile1, exceptionChainProfile2);
                    } else {
                        if (getCaughtExceptionNode == null) {
                            CompilerDirectives.transferToInterpreterAndInvalidate();
                            getCaughtExceptionNode = ExceptionStateNodes.GetCaughtExceptionNode.create();
                        }
                        PException exceptionState = getCaughtExceptionNode.execute(virtualFrame);
                        if (exceptionState != null) {
                            ExceptionHandlingStatementNode.chainExceptions(pe.getUnreifiedException(), exceptionState, exceptionChainProfile1, exceptionChainProfile2);
                        }
                    }
                }
                if (targetIndex == -1) {
                    // For tracebacks
                    setCurrentBci(virtualFrame, bciSlot, beginBci);
                    if (isGeneratorOrCoroutine) {
                        if (localFrame != virtualFrame) {
                            // Unwind the generator frame stack
                            clearFrameSlots(localFrame, stackoffset, initialStackTop);
                        }
                    }
                    if (CompilerDirectives.hasNextTier() && mutableData.loopCount > 0) {
                        LoopNode.reportLoopCount(this, mutableData.loopCount);
                    }
                    if (e == pe) {
                        throw pe;
                    } else if (pe != null) {
                        throw pe.getExceptionForReraise();
                    } else {
                        throw e;
                    }
                } else {
                    if (pe != null) {
                        pe.setCatchingFrameReference(virtualFrame, this, bci);
                    }
                    int stackSizeOnEntry = exceptionHandlerRanges[targetIndex + 1];
                    stackTop = unwindBlock(virtualFrame, stackTop, stackSizeOnEntry + stackoffset);
                    // handler range encodes the stack size, not the top of stack. so the stackTop
                    // is
                    // to be replaced with the exception
                    virtualFrame.setObject(stackTop, isInteropException ? e : pe);
                    bci = exceptionHandlerRanges[targetIndex];
                    oparg = 0;
                }
            }
        }
    }

    private static void setCurrentBci(VirtualFrame virtualFrame, int bciSlot, int bci) {
        virtualFrame.setIntStatic(bciSlot, bci);
    }

    private boolean bytecodePopCondition(VirtualFrame virtualFrame, int stackTop, Node[] localNodes, int bci, boolean useCachedNodes) {
        PyObjectIsTrueNode isTrue = insertChildNode(localNodes, bci, UNCACHED_OBJECT_IS_TRUE, PyObjectIsTrueNodeGen.class, NODE_OBJECT_IS_TRUE, useCachedNodes);
        Object cond;
        try {
            cond = virtualFrame.getObject(stackTop);
        } catch (FrameSlotTypeException e) {
            // This should only happen when quickened concurrently in multi-context mode
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeInputs(bci);
            cond = virtualFrame.getValue(stackTop);
        }
        virtualFrame.setObject(stackTop, null);
        return isTrue.execute(virtualFrame, cond);
    }

    private void bytecodeBinaryOpAdaptive(VirtualFrame virtualFrame, int stackTop, byte[] localBC, int bci, Node[] localNodes, int op, boolean useCachedNodes) {
        if (virtualFrame.isObject(stackTop) && virtualFrame.isObject(stackTop - 1)) {
            localBC[bci] = OpCodesConstants.BINARY_OP_OO_O;
            bytecodeBinaryOpOOO(virtualFrame, stackTop, bci, localNodes, op, bcioffset);
            return;
        } else if (virtualFrame.isInt(stackTop) && virtualFrame.isInt(stackTop - 1)) {
            switch (op) {
                case BinaryOpsConstants.ADD:
                case BinaryOpsConstants.INPLACE_ADD:
                case BinaryOpsConstants.SUB:
                case BinaryOpsConstants.INPLACE_SUB:
                case BinaryOpsConstants.MUL:
                case BinaryOpsConstants.INPLACE_MUL:
                case BinaryOpsConstants.FLOORDIV:
                case BinaryOpsConstants.INPLACE_FLOORDIV:
                case BinaryOpsConstants.MOD:
                case BinaryOpsConstants.INPLACE_MOD:
                case BinaryOpsConstants.LSHIFT:
                case BinaryOpsConstants.INPLACE_LSHIFT:
                case BinaryOpsConstants.RSHIFT:
                case BinaryOpsConstants.INPLACE_RSHIFT:
                case BinaryOpsConstants.AND:
                case BinaryOpsConstants.INPLACE_AND:
                case BinaryOpsConstants.OR:
                case BinaryOpsConstants.INPLACE_OR:
                case BinaryOpsConstants.XOR:
                case BinaryOpsConstants.INPLACE_XOR:
                    if ((outputCanQuicken[bci] & QuickeningTypes.INT) != 0) {
                        localBC[bci] = OpCodesConstants.BINARY_OP_II_I;
                        bytecodeBinaryOpIII(virtualFrame, stackTop, bci, localNodes, op, useCachedNodes);
                    } else {
                        localBC[bci] = OpCodesConstants.BINARY_OP_II_O;
                        bytecodeBinaryOpIIO(virtualFrame, stackTop, bci, localNodes, op);
                    }
                    return;
                case BinaryOpsConstants.TRUEDIV:
                case BinaryOpsConstants.INPLACE_TRUEDIV:
                    // TODO truediv should quicken to BINARY_OP_II_D
                    localBC[bci] = OpCodesConstants.BINARY_OP_II_O;
                    bytecodeBinaryOpIIO(virtualFrame, stackTop, bci, localNodes, op);
                    return;
                case BinaryOpsConstants.EQ:
                case BinaryOpsConstants.NE:
                case BinaryOpsConstants.GT:
                case BinaryOpsConstants.GE:
                case BinaryOpsConstants.LE:
                case BinaryOpsConstants.LT:
                case BinaryOpsConstants.IS:
                    if ((outputCanQuicken[bci] & QuickeningTypes.BOOLEAN) != 0) {
                        localBC[bci] = OpCodesConstants.BINARY_OP_II_B;
                        bytecodeBinaryOpIIB(virtualFrame, stackTop, bci, op);
                    } else {
                        localBC[bci] = OpCodesConstants.BINARY_OP_II_O;
                        bytecodeBinaryOpIIO(virtualFrame, stackTop, bci, localNodes, op);
                    }
                    return;
                case BinaryOpsConstants.POW:
                case BinaryOpsConstants.INPLACE_POW:
                    // TODO we should add at least a long version of pow
                    break;
            }
        }
        // TODO other types
        virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
        virtualFrame.setObject(stackTop - 1, virtualFrame.getValue(stackTop - 1));
        generalizeInputs(bci);
        localBC[bci] = OpCodesConstants.BINARY_OP_OO_O;
        bytecodeBinaryOpOOO(virtualFrame, stackTop, bci, localNodes, op, bcioffset);
    }

    private void bytecodeBinaryOpIIB(VirtualFrame virtualFrame, int stackTop, int bci, int op) {
        int right, left;
        if (virtualFrame.isInt(stackTop) && virtualFrame.isInt(stackTop - 1)) {
            right = virtualFrame.getInt(stackTop);
            left = virtualFrame.getInt(stackTop - 1);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
            virtualFrame.setObject(stackTop - 1, virtualFrame.getValue(stackTop - 1));
            generalizeInputs(bci);
            bytecode[bci] = OpCodesConstants.BINARY_OP_OO_O;
            bytecodeBinaryOpOOO(virtualFrame, stackTop, bci, adoptedNodes, op, bcioffset);
            return;
        }
        boolean result;
        switch (op) {
            case BinaryOpsConstants.EQ:
            case BinaryOpsConstants.IS:
                result = left == right;
                break;
            case BinaryOpsConstants.NE:
                result = left != right;
                break;
            case BinaryOpsConstants.LT:
                result = left < right;
                break;
            case BinaryOpsConstants.LE:
                result = left <= right;
                break;
            case BinaryOpsConstants.GT:
                result = left > right;
                break;
            case BinaryOpsConstants.GE:
                result = left >= right;
                break;
            default:
                throw CompilerDirectives.shouldNotReachHere("Invalid operation for BINARY_OP_II_B");
        }
        virtualFrame.setBoolean(stackTop - 1, result);
    }

    private void bytecodeBinaryOpIIO(VirtualFrame virtualFrame, int stackTop, int bci, Node[] localNodes, int op) {
        int right, left;
        if (virtualFrame.isInt(stackTop) && virtualFrame.isInt(stackTop - 1)) {
            right = virtualFrame.getInt(stackTop);
            left = virtualFrame.getInt(stackTop - 1);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
            virtualFrame.setObject(stackTop - 1, virtualFrame.getValue(stackTop - 1));
            generalizeInputs(bci);
            bytecode[bci] = OpCodesConstants.BINARY_OP_OO_O;
            bytecodeBinaryOpOOO(virtualFrame, stackTop, bci, localNodes, op, bcioffset);
            return;
        }
        Object result;
        switch (op) {
            case BinaryOpsConstants.ADD:
            case BinaryOpsConstants.INPLACE_ADD:
                IntBuiltins.AddNode addNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.AddNodeFactory.AddNodeGen.class, NODE_INT_ADD);
                result = addNode.execute(left, right);
                break;
            case BinaryOpsConstants.SUB:
            case BinaryOpsConstants.INPLACE_SUB:
                IntBuiltins.SubNode subNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.SubNodeFactory.SubNodeGen.class, NODE_INT_SUB);
                result = subNode.execute(left, right);
                break;
            case BinaryOpsConstants.MUL:
            case BinaryOpsConstants.INPLACE_MUL:
                IntBuiltins.MulNode mulNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.MulNodeFactory.MulNodeGen.class, NODE_INT_MUL);
                result = mulNode.execute(left, right);
                break;
            case BinaryOpsConstants.FLOORDIV:
            case BinaryOpsConstants.INPLACE_FLOORDIV:
                IntBuiltins.FloorDivNode floorDivNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.FloorDivNodeFactory.FloorDivNodeGen.class, NODE_INT_FLOORDIV);
                result = floorDivNode.execute(left, right);
                break;
            case BinaryOpsConstants.TRUEDIV:
            case BinaryOpsConstants.INPLACE_TRUEDIV:
                IntBuiltins.TrueDivNode trueDivNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.TrueDivNodeFactory.TrueDivNodeGen.class, NODE_INT_TRUEDIV);
                result = trueDivNode.execute(left, right);
                break;
            case BinaryOpsConstants.MOD:
            case BinaryOpsConstants.INPLACE_MOD:
                IntBuiltins.ModNode modNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.ModNodeFactory.ModNodeGen.class, NODE_INT_MOD);
                result = modNode.execute(left, right);
                break;
            case BinaryOpsConstants.LSHIFT:
            case BinaryOpsConstants.INPLACE_LSHIFT:
                IntBuiltins.LShiftNode lShiftNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.LShiftNodeFactory.LShiftNodeGen.class, NODE_INT_LSHIFT);
                result = lShiftNode.execute(left, right);
                break;
            case BinaryOpsConstants.RSHIFT:
            case BinaryOpsConstants.INPLACE_RSHIFT:
                IntBuiltins.RShiftNode rShiftNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.RShiftNodeFactory.RShiftNodeGen.class, NODE_INT_RSHIFT);
                result = rShiftNode.execute(left, right);
                break;
            case BinaryOpsConstants.AND:
            case BinaryOpsConstants.INPLACE_AND:
                result = left & right;
                break;
            case BinaryOpsConstants.OR:
            case BinaryOpsConstants.INPLACE_OR:
                result = left | right;
                break;
            case BinaryOpsConstants.XOR:
            case BinaryOpsConstants.INPLACE_XOR:
                result = left ^ right;
                break;
            case BinaryOpsConstants.IS:
            case BinaryOpsConstants.EQ:
                result = left == right;
                break;
            case BinaryOpsConstants.NE:
                result = left != right;
                break;
            case BinaryOpsConstants.LT:
                result = left < right;
                break;
            case BinaryOpsConstants.LE:
                result = left <= right;
                break;
            case BinaryOpsConstants.GT:
                result = left > right;
                break;
            case BinaryOpsConstants.GE:
                result = left >= right;
                break;
            default:
                throw CompilerDirectives.shouldNotReachHere("Invalid operation for BINARY_OP_II_O");
        }
        virtualFrame.setObject(stackTop, null);
        virtualFrame.setObject(stackTop - 1, result);
    }

    private void bytecodeBinaryOpIII(VirtualFrame virtualFrame, int stackTop, int bci, Node[] localNodes, int op, boolean useCachedNodes) {
        int right, left, result;
        if (virtualFrame.isInt(stackTop) && virtualFrame.isInt(stackTop - 1)) {
            right = virtualFrame.getInt(stackTop);
            left = virtualFrame.getInt(stackTop - 1);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
            virtualFrame.setObject(stackTop - 1, virtualFrame.getValue(stackTop - 1));
            generalizeInputs(bci);
            bytecode[bci] = OpCodesConstants.BINARY_OP_OO_O;
            bytecodeBinaryOpOOO(virtualFrame, stackTop, bci, adoptedNodes, op, bcioffset);
            return;
        }
        try {
            switch (op) {
                case BinaryOpsConstants.ADD:
                case BinaryOpsConstants.INPLACE_ADD:
                    try {
                        result = PythonUtils.addExact(left, right);
                    } catch (OverflowException e) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        bytecode[bci] = OpCodesConstants.BINARY_OP_II_O;
                        bytecodeBinaryOpIIO(virtualFrame, stackTop, bci, adoptedNodes, op);
                        return;
                    }
                    break;
                case BinaryOpsConstants.SUB:
                case BinaryOpsConstants.INPLACE_SUB:
                    try {
                        result = PythonUtils.subtractExact(left, right);
                    } catch (OverflowException e) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        bytecode[bci] = OpCodesConstants.BINARY_OP_II_O;
                        bytecodeBinaryOpIIO(virtualFrame, stackTop, bci, adoptedNodes, op);
                        return;
                    }
                    break;
                case BinaryOpsConstants.MUL:
                case BinaryOpsConstants.INPLACE_MUL:
                    try {
                        result = PythonUtils.multiplyExact(left, right);
                    } catch (OverflowException e) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        bytecode[bci] = OpCodesConstants.BINARY_OP_II_O;
                        bytecodeBinaryOpIIO(virtualFrame, stackTop, bci, adoptedNodes, op);
                        return;
                    }
                    break;
                case BinaryOpsConstants.FLOORDIV:
                case BinaryOpsConstants.INPLACE_FLOORDIV:
                    if (left == Integer.MIN_VALUE && right == -1) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        bytecode[bci] = OpCodesConstants.BINARY_OP_II_O;
                        bytecodeBinaryOpIIO(virtualFrame, stackTop, bci, adoptedNodes, op);
                        return;
                    }
                    if (right == 0) {
                        PRaiseNode raiseNode = insertChildNode(localNodes, bci, UNCACHED_RAISE, PRaiseNodeGen.class, NODE_RAISE, useCachedNodes);
                        throw raiseNode.raise(ZeroDivisionError, ErrorMessages.S_DIVISION_OR_MODULO_BY_ZERO, "integer");
                    }
                    result = Math.floorDiv(left, right);
                    break;
                case BinaryOpsConstants.MOD:
                case BinaryOpsConstants.INPLACE_MOD:
                    IntBuiltins.ModNode modNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.ModNodeFactory.ModNodeGen.class, NODE_INT_MOD);
                    result = modNode.executeInt(left, right);
                    break;
                case BinaryOpsConstants.LSHIFT:
                case BinaryOpsConstants.INPLACE_LSHIFT:
                    IntBuiltins.LShiftNode lShiftNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.LShiftNodeFactory.LShiftNodeGen.class, NODE_INT_LSHIFT);
                    result = lShiftNode.executeInt(left, right);
                    break;
                case BinaryOpsConstants.RSHIFT:
                case BinaryOpsConstants.INPLACE_RSHIFT:
                    IntBuiltins.RShiftNode rShiftNode = insertChildNode(localNodes, bci, IntBuiltinsFactory.RShiftNodeFactory.RShiftNodeGen.class, NODE_INT_RSHIFT);
                    result = rShiftNode.executeInt(left, right);
                    break;
                case BinaryOpsConstants.AND:
                case BinaryOpsConstants.INPLACE_AND:
                    result = left & right;
                    break;
                case BinaryOpsConstants.OR:
                case BinaryOpsConstants.INPLACE_OR:
                    result = left | right;
                    break;
                case BinaryOpsConstants.XOR:
                case BinaryOpsConstants.INPLACE_XOR:
                    result = left ^ right;
                    break;
                default:
                    throw CompilerDirectives.shouldNotReachHere("Invalid operation for BINARY_OP_II_O");
            }
        } catch (UnexpectedResultException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            bytecode[bci] = OpCodesConstants.BINARY_OP_II_O;
            bytecodeBinaryOpIIO(virtualFrame, stackTop, bci, adoptedNodes, op);
            return;
        }
        virtualFrame.setInt(stackTop - 1, result);
    }

    private void bytecodeBinaryOpOOO(VirtualFrame virtualFrame, int stackTop, int bci, Node[] localNodes, int op, int bciSlot) {
        setCurrentBci(virtualFrame, bciSlot, bci);
        BinaryOp opNode = (BinaryOp) insertChildNodeInt(localNodes, bci, BinaryOp.class, BINARY_OP_FACTORY, op);
        Object right, left;
        try {
            right = virtualFrame.getObject(stackTop);
            left = virtualFrame.getObject(stackTop - 1);
        } catch (FrameSlotTypeException e) {
            // This should only happen when quickened concurrently in multi-context
            // mode
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeInputs(bci);
            right = virtualFrame.getValue(stackTop);
            left = virtualFrame.getValue(stackTop - 1);
        }
        virtualFrame.setObject(stackTop, null);
        Object result = opNode.executeObject(virtualFrame, left, right);
        virtualFrame.setObject(stackTop - 1, result);
    }

    private void bytecodeUnaryOpAdaptive(VirtualFrame virtualFrame, int stackTop, int bci, byte[] localBC, Node[] localNodes) {
        int op = Byte.toUnsignedInt(localBC[bci + 1]);
        if (virtualFrame.isObject(stackTop)) {
            localBC[bci] = OpCodesConstants.UNARY_OP_O_O;
            bytecodeUnaryOpOO(virtualFrame, stackTop, bci, localNodes, op, bcioffset);
            return;
        } else if (virtualFrame.isInt(stackTop)) {
            if ((outputCanQuicken[bci] & QuickeningTypes.INT) != 0) {
                if (op == UnaryOpsConstants.NOT) {
                    // TODO UNARY_OP_I_B
                    localBC[bci] = OpCodesConstants.UNARY_OP_I_O;
                    bytecodeUnaryOpIO(virtualFrame, stackTop, bci, op);
                } else {
                    localBC[bci] = OpCodesConstants.UNARY_OP_I_I;
                    bytecodeUnaryOpII(virtualFrame, stackTop, bci, op);
                }
                return;
            }
            localBC[bci] = OpCodesConstants.UNARY_OP_I_O;
            bytecodeUnaryOpIO(virtualFrame, stackTop, bci, op);
            return;
        } else if (virtualFrame.isBoolean(stackTop)) {
            if (op == UnaryOpsConstants.NOT) {
                if ((outputCanQuicken[bci] & QuickeningTypes.BOOLEAN) != 0) {
                    localBC[bci] = OpCodesConstants.UNARY_OP_B_B;
                    bytecodeUnaryOpBB(virtualFrame, stackTop, bci, op);
                } else {
                    localBC[bci] = OpCodesConstants.UNARY_OP_B_O;
                    bytecodeUnaryOpBO(virtualFrame, stackTop, bci, op);
                }
                return;
            }
        }
        // TODO other types
        generalizeInputs(bci);
        virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
        localBC[bci] = OpCodesConstants.UNARY_OP_O_O;
        bytecodeUnaryOpOO(virtualFrame, stackTop, bci, localNodes, op, bcioffset);
    }

    private void bytecodeUnaryOpII(VirtualFrame virtualFrame, int stackTop, int bci, int op) {
        int value;
        if (virtualFrame.isInt(stackTop)) {
            value = virtualFrame.getInt(stackTop);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
            generalizeInputs(bci);
            bytecode[bci] = OpCodesConstants.UNARY_OP_O_O;
            bytecodeUnaryOpOO(virtualFrame, stackTop, bci, adoptedNodes, op, bcioffset);
            return;
        }
        switch (op) {
            case UnaryOpsConstants.POSITIVE:
                break;
            case UnaryOpsConstants.NEGATIVE:
                if (value == Integer.MIN_VALUE) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    bytecode[bci] = OpCodesConstants.UNARY_OP_I_O;
                    bytecodeUnaryOpIO(virtualFrame, stackTop, bci, op);
                    return;
                }
                virtualFrame.setInt(stackTop, -value);
                break;
            case UnaryOpsConstants.INVERT:
                virtualFrame.setInt(stackTop, ~value);
                break;
            default:
                throw CompilerDirectives.shouldNotReachHere("Invalid operation for UNARY_OP_I_I");
        }
    }

    private void bytecodeUnaryOpIO(VirtualFrame virtualFrame, int stackTop, int bci, int op) {
        int value;
        if (virtualFrame.isInt(stackTop)) {
            value = virtualFrame.getInt(stackTop);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
            generalizeInputs(bci);
            bytecode[bci] = OpCodesConstants.UNARY_OP_O_O;
            bytecodeUnaryOpOO(virtualFrame, stackTop, bci, adoptedNodes, op, bcioffset);
            return;
        }
        Object result;
        switch (op) {
            case UnaryOpsConstants.NOT:
                result = value == 0;
                break;
            case UnaryOpsConstants.POSITIVE:
                result = value;
                break;
            case UnaryOpsConstants.NEGATIVE:
                if (value != Integer.MIN_VALUE) {
                    result = -value;
                } else {
                    result = -(long) value;
                }
                break;
            case UnaryOpsConstants.INVERT:
                result = ~value;
                break;
            default:
                throw CompilerDirectives.shouldNotReachHere("Invalid operation for UNARY_OP_I_O");
        }
        virtualFrame.setObject(stackTop, result);
    }

    private void bytecodeUnaryOpBB(VirtualFrame virtualFrame, int stackTop, int bci, int op) {
        boolean value;
        if (virtualFrame.isBoolean(stackTop)) {
            value = virtualFrame.getBoolean(stackTop);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
            generalizeInputs(bci);
            bytecode[bci] = OpCodesConstants.UNARY_OP_O_O;
            bytecodeUnaryOpOO(virtualFrame, stackTop, bci, adoptedNodes, op, bcioffset);
            return;
        }
        if (op == UnaryOpsConstants.NOT) {
            virtualFrame.setBoolean(stackTop, !value);
        } else {
            throw CompilerDirectives.shouldNotReachHere("Invalid operation for UNARY_OP_B_B");
        }
    }

    private void bytecodeUnaryOpBO(VirtualFrame virtualFrame, int stackTop, int bci, int op) {
        boolean value;
        if (virtualFrame.isBoolean(stackTop)) {
            value = virtualFrame.getBoolean(stackTop);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
            generalizeInputs(bci);
            bytecode[bci] = OpCodesConstants.UNARY_OP_O_O;
            bytecodeUnaryOpOO(virtualFrame, stackTop, bci, adoptedNodes, op, bcioffset);
            return;
        }
        if (op == UnaryOpsConstants.NOT) {
            virtualFrame.setObject(stackTop, !value);
        } else {
            throw CompilerDirectives.shouldNotReachHere("Invalid operation for UNARY_OP_B_B");
        }
    }

    private void bytecodeUnaryOpOO(VirtualFrame virtualFrame, int stackTop, int bci, Node[] localNodes, int op, int bciSlot) {
        setCurrentBci(virtualFrame, bciSlot, bci);
        UnaryOpNode opNode = insertChildNodeInt(localNodes, bci, UnaryOpNode.class, UNARY_OP_FACTORY, op);
        Object value;
        try {
            value = virtualFrame.getObject(stackTop);
        } catch (FrameSlotTypeException e) {
            // This should only happen when quickened concurrently in multi-context
            // mode
            generalizeInputs(bci);
            value = virtualFrame.getValue(stackTop);
        }
        Object result = opNode.execute(virtualFrame, value);
        virtualFrame.setObject(stackTop, result);
    }

    private void bytecodeStoreFastAdaptive(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, byte[] localBC, int index) {
        byte stackType = stackSlotTypeToTypeId(virtualFrame, stackTop);
        byte itemType = stackType;
        if (itemType == QuickeningTypes.OBJECT && variableShouldUnbox[index] != 0) {
            itemType = objectTypeId(virtualFrame.getObject(stackTop));
            itemType &= variableShouldUnbox[index] | QuickeningTypes.OBJECT;
        }
        if (variableTypes[index] == 0) {
            variableTypes[index] = itemType;
        } else if (variableTypes[index] != itemType) {
            if (variableTypes[index] != QuickeningTypes.OBJECT) {
                variableTypes[index] = QuickeningTypes.OBJECT;
                generalizeVariableStores(index);
            }
            if (itemType != QuickeningTypes.OBJECT) {
                generalizeInputs(bci);
                virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
            }
            localBC[bci] = OpCodesConstants.STORE_FAST_O;
            bytecodeStoreFastO(virtualFrame, localFrame, stackTop, index);
            return;
        }
        assert variableTypes[index] == itemType;
        if (itemType == QuickeningTypes.INT) {
            if (stackType == QuickeningTypes.INT) {
                localBC[bci] = OpCodesConstants.STORE_FAST_I;
                bytecodeStoreFastI(virtualFrame, localFrame, stackTop, bci, index);
            } else {
                localBC[bci] = OpCodesConstants.STORE_FAST_UNBOX_I;
                bytecodeStoreFastUnboxI(virtualFrame, localFrame, stackTop, bci, index);
            }
            return;
        } else if (itemType == QuickeningTypes.BOOLEAN) {
            if (stackType == QuickeningTypes.BOOLEAN) {
                localBC[bci] = OpCodesConstants.STORE_FAST_B;
                bytecodeStoreFastB(virtualFrame, localFrame, stackTop, bci, index);
            } else {
                localBC[bci] = OpCodesConstants.STORE_FAST_UNBOX_B;
                bytecodeStoreFastUnboxB(virtualFrame, localFrame, stackTop, bci, index);
            }
            return;
        } else if (itemType == QuickeningTypes.OBJECT) {
            localBC[bci] = OpCodesConstants.STORE_FAST_O;
            bytecodeStoreFastO(virtualFrame, localFrame, stackTop, index);
            return;
        }
        // TODO other types
        generalizeInputs(bci);
        generalizeVariableStores(index);
        virtualFrame.setObject(stackTop, virtualFrame.getValue(stackTop));
        localBC[bci] = OpCodesConstants.STORE_FAST_O;
        bytecodeStoreFastO(virtualFrame, localFrame, stackTop, index);
    }

    private void bytecodeStoreFastI(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, int index) {
        if (virtualFrame.isInt(stackTop)) {
            localFrame.setInt(index, virtualFrame.getInt(stackTop));
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            bytecode[bci] = OpCodesConstants.STORE_FAST_O;
            generalizeVariableStores(index);
            bytecodeStoreFastO(virtualFrame, localFrame, stackTop, index);
        }
    }

    private void bytecodeStoreFastUnboxI(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, int index) {
        Object object;
        try {
            object = virtualFrame.getObject(stackTop);
        } catch (FrameSlotTypeException e) {
            // This should only happen when quickened concurrently in multi-context
            // mode
            generalizeVariableStores(index);
            object = virtualFrame.getValue(stackTop);
        }
        if (object instanceof Integer) {
            localFrame.setInt(index, (int) object);
            virtualFrame.setObject(stackTop, null);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            bytecode[bci] = OpCodesConstants.STORE_FAST_O;
            generalizeInputs(bci);
            bytecodeStoreFastO(virtualFrame, localFrame, stackTop, index);
        }
    }

    private void bytecodeStoreFastB(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, int index) {
        if (virtualFrame.isBoolean(stackTop)) {
            localFrame.setBoolean(index, virtualFrame.getBoolean(stackTop));
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            bytecode[bci] = OpCodesConstants.STORE_FAST_O;
            generalizeVariableStores(index);
            bytecodeStoreFastO(virtualFrame, localFrame, stackTop, index);
        }
    }

    private void bytecodeStoreFastUnboxB(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, int index) {
        Object object;
        try {
            object = virtualFrame.getObject(stackTop);
        } catch (FrameSlotTypeException e) {
            // This should only happen when quickened concurrently in multi-context
            // mode
            generalizeVariableStores(index);
            object = virtualFrame.getValue(stackTop);
        }
        if (object instanceof Boolean) {
            localFrame.setBoolean(index, (boolean) object);
            virtualFrame.setObject(stackTop, null);
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            bytecode[bci] = OpCodesConstants.STORE_FAST_O;
            generalizeInputs(bci);
            bytecodeStoreFastO(virtualFrame, localFrame, stackTop, index);
        }
    }

    private void bytecodeStoreFastO(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int index) {
        Object object;
        try {
            object = virtualFrame.getObject(stackTop);
        } catch (FrameSlotTypeException e) {
            // This should only happen when quickened concurrently in multi-context
            // mode
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeVariableStores(index);
            object = virtualFrame.getValue(stackTop);
        }
        localFrame.setObject(index, object);
        virtualFrame.setObject(stackTop, null);
    }

    private void bytecodeLoadFastAdaptive(VirtualFrame virtualFrame, Frame localFrame, int stackTop, byte[] localBC, int bci, int index) {
        if (localFrame.isObject(index)) {
            localBC[bci] = OpCodesConstants.LOAD_FAST_O;
            bytecodeLoadFastO(virtualFrame, localFrame, stackTop, bci, index);
        } else if (localFrame.isInt(index)) {
            if ((outputCanQuicken[bci] & QuickeningTypes.INT) != 0) {
                localBC[bci] = OpCodesConstants.LOAD_FAST_I;
                bytecodeLoadFastI(virtualFrame, localFrame, stackTop, bci, index);
            } else {
                localBC[bci] = OpCodesConstants.LOAD_FAST_I_BOX;
                bytecodeLoadFastIBox(virtualFrame, localFrame, stackTop, bci, index);
            }
        } else if (localFrame.isBoolean(index)) {
            if ((outputCanQuicken[bci] & QuickeningTypes.BOOLEAN) != 0) {
                localBC[bci] = OpCodesConstants.LOAD_FAST_B;
                bytecodeLoadFastB(virtualFrame, localFrame, stackTop, bci, index);
            } else {
                localBC[bci] = OpCodesConstants.LOAD_FAST_B_BOX;
                bytecodeLoadFastBBox(virtualFrame, localFrame, stackTop, bci, index);
            }
        } else {
            throw CompilerDirectives.shouldNotReachHere("Unimplemented stack item type for LOAD_FAST");
        }
    }

    private void bytecodeLoadFastIBox(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, int index) {
        if (localFrame.isInt(index)) {
            virtualFrame.setObject(stackTop, localFrame.getInt(index));
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeVariableStores(index);
            bytecode[bci] = OpCodesConstants.LOAD_FAST_O;
            bytecodeLoadFastO(virtualFrame, localFrame, stackTop, bci, index);
        }
    }

    private void bytecodeLoadFastI(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, int index) {
        if (localFrame.isInt(index)) {
            virtualFrame.setInt(stackTop, localFrame.getInt(index));
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeVariableStores(index);
            bytecode[bci] = OpCodesConstants.LOAD_FAST_O;
            bytecodeLoadFastO(virtualFrame, localFrame, stackTop, bci, index);
        }
    }

    private void bytecodeLoadFastBBox(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, int index) {
        if (localFrame.isBoolean(index)) {
            virtualFrame.setObject(stackTop, localFrame.getBoolean(index));
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeVariableStores(index);
            bytecode[bci] = OpCodesConstants.LOAD_FAST_O;
            bytecodeLoadFastO(virtualFrame, localFrame, stackTop, bci, index);
        }
    }

    private void bytecodeLoadFastB(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, int index) {
        if (localFrame.isBoolean(index)) {
            virtualFrame.setBoolean(stackTop, localFrame.getBoolean(index));
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeVariableStores(index);
            bytecode[bci] = OpCodesConstants.LOAD_FAST_O;
            bytecodeLoadFastO(virtualFrame, localFrame, stackTop, bci, index);
        }
    }

    private void bytecodeLoadFastO(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, int index) {
        Object value;
        try {
            value = localFrame.getObject(index);
        } catch (FrameSlotTypeException e) {
            // This should only happen when quickened concurrently in multi-context
            // mode
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeVariableStores(index);
            value = localFrame.getValue(index);
        }
        if (value == null) {
            PRaiseNode raiseNode = insertChildNode(adoptedNodes, bci, PRaiseNodeGen.class, NODE_RAISE);
            throw raiseNode.raise(PythonBuiltinClassType.UnboundLocalError, ErrorMessages.LOCAL_VAR_REFERENCED_BEFORE_ASSIGMENT, varnames[index]);
        }
        virtualFrame.setObject(stackTop, value);
    }

    private byte stackSlotTypeToTypeId(VirtualFrame virtualFrame, int stackTop) {
        if (virtualFrame.isObject(stackTop)) {
            return QuickeningTypes.OBJECT;
        } else if (virtualFrame.isInt(stackTop)) {
            return QuickeningTypes.INT;
        } else if (virtualFrame.isLong(stackTop)) {
            return QuickeningTypes.LONG;
        } else if (virtualFrame.isDouble(stackTop)) {
            return QuickeningTypes.DOUBLE;
        } else if (virtualFrame.isBoolean(stackTop)) {
            return QuickeningTypes.BOOLEAN;
        } else {
            throw CompilerDirectives.shouldNotReachHere("Unknown stack item type");
        }
    }

    private byte objectTypeId(Object object) {
        if (object instanceof Integer) {
            return QuickeningTypes.INT;
        } else if (object instanceof Long) {
            return QuickeningTypes.LONG;
        } else if (object instanceof Double) {
            return QuickeningTypes.DOUBLE;
        } else if (object instanceof Boolean) {
            return QuickeningTypes.BOOLEAN;
        } else {
            return QuickeningTypes.OBJECT;
        }
    }

    private void generalizeInputs(int beginBci) {
        CompilerAsserts.neverPartOfCompilation();
        if (generalizeInputsMap != null) {
            if (generalizeInputsMap[beginBci] != null) {
                for (int i = 0; i < generalizeInputsMap[beginBci].length; i++) {
                    int generalizeBci = generalizeInputsMap[beginBci][i];
                    OpCodes generalizeInstr = OpCodes.VALUES[bytecode[generalizeBci]];
                    if (generalizeInstr.generalizesTo != null) {
                        bytecode[generalizeBci] = (byte) generalizeInstr.generalizesTo.ordinal();
                    }
                }
            }
        }
    }

    private void generalizeVariableStores(int index) {
        CompilerAsserts.neverPartOfCompilation();
        variableTypes[index] = QuickeningTypes.OBJECT;
        if (generalizeVarsMap != null) {
            if (generalizeVarsMap[index] != null) {
                for (int i = 0; i < generalizeVarsMap[index].length; i++) {
                    int generalizeBci = generalizeVarsMap[index][i];
                    /*
                     * Keep unadapted stores as they are because we don't know how to generalize
                     * their unadapted inputs. They will adapt to object once executed.
                     */
                    if (bytecode[generalizeBci] != OpCodesConstants.STORE_FAST) {
                        generalizeInputs(generalizeBci);
                        bytecode[generalizeBci] = OpCodesConstants.STORE_FAST_O;
                    }
                }
            }
        }
    }

    protected PException wrapJavaExceptionIfApplicable(Throwable e) {
        if (e instanceof AbstractTruffleException) {
            return null;
        }
        if (e instanceof ControlFlowException) {
            return null;
        }
        if (PythonLanguage.get(this).getEngineOption(PythonOptions.CatchAllExceptions) && (e instanceof Exception || e instanceof AssertionError)) {
            return wrapJavaException(e, factory.createBaseException(SystemError, ErrorMessages.M, new Object[]{e}));
        }
        if (e instanceof StackOverflowError) {
            PythonContext.get(this).reacquireGilAfterStackOverflow();
            return wrapJavaException(e, factory.createBaseException(RecursionError, ErrorMessages.MAXIMUM_RECURSION_DEPTH_EXCEEDED, new Object[]{}));
        }
        return null;
    }

    public PException wrapJavaException(Throwable e, PBaseException pythonException) {
        PException pe = PException.fromObject(pythonException, this, e);
        pe.setHideLocation(true);
        // Host exceptions have their stacktrace already filled in, call this to set
        // the cutoff point to the catch site
        pe.getTruffleStackTrace();
        return pe;
    }

    @ExplodeLoop
    private void copyStackSlotsToGeneratorFrame(Frame virtualFrame, Frame generatorFrame, int stackTop) {
        for (int i = stackoffset; i <= stackTop; i++) {
            if (virtualFrame.isObject(i)) {
                generatorFrame.setObject(i, virtualFrame.getObject(i));
            } else if (virtualFrame.isInt(i)) {
                generatorFrame.setInt(i, virtualFrame.getInt(i));
            } else if (virtualFrame.isLong(i)) {
                generatorFrame.setLong(i, virtualFrame.getLong(i));
            } else if (virtualFrame.isDouble(i)) {
                generatorFrame.setDouble(i, virtualFrame.getDouble(i));
            } else if (virtualFrame.isBoolean(i)) {
                generatorFrame.setBoolean(i, virtualFrame.getBoolean(i));
            } else {
                throw CompilerDirectives.shouldNotReachHere("unexpected frame slot type");
            }
        }
    }

    @ExplodeLoop
    private void clearFrameSlots(Frame frame, int start, int end) {
        CompilerAsserts.partialEvaluationConstant(start);
        CompilerAsserts.partialEvaluationConstant(end);
        for (int i = start; i <= end; i++) {
            frame.setObject(i, null);
        }
    }

    private int bytecodeFormatValue(VirtualFrame virtualFrame, int initialStackTop, int bci, Node[] localNodes, int options, boolean useCachedNodes) {
        int stackTop = initialStackTop;
        int type = options & FormatOptions.FVC_MASK;
        Object spec = PNone.NO_VALUE;
        if ((options & FormatOptions.FVS_MASK) == FormatOptions.FVS_HAVE_SPEC) {
            spec = virtualFrame.getObject(stackTop);
            virtualFrame.setObject(stackTop--, null);
        }
        Object value = virtualFrame.getObject(stackTop);
        switch (type) {
            case FormatOptions.FVC_STR:
                value = insertChildNode(localNodes, bci, UNCACHED_STR, PyObjectStrAsObjectNodeGen.class, NODE_STR, useCachedNodes).execute(virtualFrame, value);
                break;
            case FormatOptions.FVC_REPR:
                value = insertChildNode(localNodes, bci, UNCACHED_REPR, PyObjectReprAsObjectNodeGen.class, NODE_REPR, useCachedNodes).execute(virtualFrame, value);
                break;
            case FormatOptions.FVC_ASCII:
                value = insertChildNode(localNodes, bci, UNCACHED_ASCII, PyObjectAsciiNodeGen.class, NODE_ASCII, useCachedNodes).execute(virtualFrame, value);
                break;
            default:
                assert type == FormatOptions.FVC_NONE;
        }
        FormatNode formatNode = insertChildNode(localNodes, bci + 1, FormatNodeGen.class, NODE_FORMAT);
        value = formatNode.execute(virtualFrame, value, spec);
        virtualFrame.setObject(stackTop, value);
        return stackTop;
    }

    private void bytecodeDeleteDeref(Frame localFrame, int bci, Node[] localNodes, int oparg, int celloffset, boolean useCachedNodes) {
        PCell cell = (PCell) localFrame.getObject(celloffset + oparg);
        Object value = cell.getRef();
        if (value == null) {
            raiseUnboundCell(localNodes, bci, oparg, useCachedNodes);
        }
        cell.clearRef();
    }

    private int bytecodeStoreDeref(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int oparg, int celloffset) {
        PCell cell = (PCell) localFrame.getObject(celloffset + oparg);
        Object value = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        cell.setRef(value);
        return stackTop;
    }

    private int bytecodeLoadClassDeref(VirtualFrame virtualFrame, Frame localFrame, Object locals, int stackTop, int bci, Node[] localNodes, int oparg, int celloffset, boolean useCachedNodes) {
        TruffleString varName;
        boolean isCellVar;
        if (oparg < cellvars.length) {
            varName = cellvars[oparg];
            isCellVar = true;
        } else {
            varName = freevars[oparg - cellvars.length];
            isCellVar = false;
        }
        GetNameFromLocalsNode getNameFromLocals = insertChildNode(localNodes, bci, UNCACHED_GET_NAME_FROM_LOCALS, GetNameFromLocalsNodeGen.class, NODE_GET_NAME_FROM_LOCALS, useCachedNodes);
        Object value = getNameFromLocals.execute(virtualFrame, locals, varName, isCellVar);
        if (value != null) {
            virtualFrame.setObject(++stackTop, value);
            return stackTop;
        } else {
            return bytecodeLoadDeref(virtualFrame, localFrame, stackTop, bci, localNodes, oparg, celloffset, useCachedNodes);
        }
    }

    private int bytecodeLoadDeref(VirtualFrame virtualFrame, Frame localFrame, int stackTop, int bci, Node[] localNodes, int oparg, int celloffset, boolean useCachedNodes) {
        PCell cell = (PCell) localFrame.getObject(celloffset + oparg);
        Object value = cell.getRef();
        if (value == null) {
            raiseUnboundCell(localNodes, bci, oparg, useCachedNodes);
        }
        virtualFrame.setObject(++stackTop, value);
        return stackTop;
    }

    private int bytecodeClosureFromStack(VirtualFrame virtualFrame, int stackTop, int oparg) {
        PCell[] closure = new PCell[oparg];
        moveFromStack(virtualFrame, stackTop - oparg + 1, stackTop + 1, closure);
        stackTop -= oparg - 1;
        virtualFrame.setObject(stackTop, closure);
        return stackTop;
    }

    private PException popExceptionState(Object[] arguments, Object savedException, PException outerException) {
        PException localException = null;
        if (savedException instanceof PException) {
            localException = (PException) savedException;
        }
        if (savedException == null) {
            savedException = outerException;
        }
        PArguments.setException(arguments, (PException) savedException);
        return localException;
    }

    private PException bytecodeEndExcHandler(VirtualFrame virtualFrame, int stackTop) {
        Object exception = virtualFrame.getObject(stackTop);
        if (exception instanceof PException) {
            throw ((PException) exception).getExceptionForReraise();
        } else if (exception instanceof AbstractTruffleException) {
            throw (AbstractTruffleException) exception;
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw PRaiseNode.raiseUncached(this, SystemError, ErrorMessages.EXPECTED_EXCEPTION_ON_THE_STACK);
        }
    }

    private void bytecodeLoadAttr(VirtualFrame virtualFrame, int stackTop, int bci, int oparg, Node[] localNodes, TruffleString[] localNames, boolean useCachedNodes) {
        PyObjectGetAttr getAttr = insertChildNode(localNodes, bci, UNCACHED_OBJECT_GET_ATTR, PyObjectGetAttrNodeGen.class, NODE_OBJECT_GET_ATTR, useCachedNodes);
        TruffleString varname = localNames[oparg];
        Object owner = virtualFrame.getObject(stackTop);
        Object value = getAttr.execute(virtualFrame, owner, varname);
        virtualFrame.setObject(stackTop, value);
    }

    private void bytecodeDeleteFast(Frame localFrame, int bci, Node[] localNodes, int oparg, boolean useCachedNodes) {
        if (localFrame.isObject(oparg)) {
            Object value = localFrame.getObject(oparg);
            if (value == null) {
                PRaiseNode raiseNode = insertChildNode(localNodes, bci, UNCACHED_RAISE, PRaiseNodeGen.class, NODE_RAISE, useCachedNodes);
                throw raiseNode.raise(PythonBuiltinClassType.UnboundLocalError, ErrorMessages.LOCAL_VAR_REFERENCED_BEFORE_ASSIGMENT, varnames[oparg]);
            }
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            generalizeVariableStores(oparg);
        }
        localFrame.setObject(oparg, null);
    }

    private int bytecodeLoadGlobal(VirtualFrame virtualFrame, Object globals, int stackTop, int bci, TruffleString localName, Node[] localNodes, boolean useCachedNodes) {
        ReadGlobalOrBuiltinNode read = insertChildNode(localNodes, bci, UNCACHED_READ_GLOBAL_OR_BUILTIN, ReadGlobalOrBuiltinNodeGen.class, NODE_READ_GLOBAL_OR_BUILTIN, localName, useCachedNodes);
        virtualFrame.setObject(++stackTop, read.read(virtualFrame, globals, localName));
        return stackTop;
    }

    private void bytecodeDeleteGlobal(VirtualFrame virtualFrame, Object globals, int bci, int oparg, Node[] localNodes, TruffleString[] localNames) {
        TruffleString varname = localNames[oparg];
        DeleteGlobalNode deleteGlobalNode = insertChildNode(localNodes, bci, DeleteGlobalNodeGen.class, NODE_DELETE_GLOBAL, varname);
        deleteGlobalNode.executeWithGlobals(virtualFrame, globals);
    }

    private int bytecodeStoreGlobal(VirtualFrame virtualFrame, Object globals, int stackTop, int bci, int oparg, Node[] localNodes, TruffleString[] localNames, boolean useCachedNodes) {
        TruffleString varname = localNames[oparg];
        WriteGlobalNode writeGlobalNode = insertChildNode(localNodes, bci, UNCACHED_WRITE_GLOBAL, WriteGlobalNodeGen.class, NODE_WRITE_GLOBAL, varname, useCachedNodes);
        writeGlobalNode.write(virtualFrame, globals, varname, virtualFrame.getObject(stackTop));
        virtualFrame.setObject(stackTop--, null);
        return stackTop;
    }

    private int bytecodeDeleteAttr(VirtualFrame virtualFrame, int stackTop, int bci, int oparg, Node[] localNodes, TruffleString[] localNames, boolean useCachedNodes) {
        PyObjectSetAttr callNode = insertChildNode(localNodes, bci, UNCACHED_OBJECT_SET_ATTR, PyObjectSetAttrNodeGen.class, NODE_OBJECT_SET_ATTR, useCachedNodes);
        TruffleString varname = localNames[oparg];
        Object owner = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        callNode.delete(virtualFrame, owner, varname);
        return stackTop;
    }

    private int bytecodeStoreAttr(VirtualFrame virtualFrame, int stackTop, int bci, int oparg, Node[] localNodes, TruffleString[] localNames, boolean useCachedNodes) {
        PyObjectSetAttr callNode = insertChildNode(localNodes, bci, UNCACHED_OBJECT_SET_ATTR, PyObjectSetAttrNodeGen.class, NODE_OBJECT_SET_ATTR, useCachedNodes);
        TruffleString varname = localNames[oparg];
        Object owner = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        Object value = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        callNode.execute(virtualFrame, owner, varname, value);
        return stackTop;
    }

    private void bytecodeDeleteName(VirtualFrame virtualFrame, Object globals, Object locals, int bci, int oparg, TruffleString[] localNames, Node[] localNodes, boolean useCachedNodes) {
        TruffleString varname = localNames[oparg];
        if (locals != null) {
            PyObjectDelItem delItemNode = insertChildNode(localNodes, bci, UNCACHED_OBJECT_DEL_ITEM, PyObjectDelItemNodeGen.class, NODE_OBJECT_DEL_ITEM, useCachedNodes);
            delItemNode.execute(virtualFrame, locals, varname);
        } else {
            DeleteGlobalNode deleteGlobalNode = insertChildNode(localNodes, bci + 1, DeleteGlobalNodeGen.class, NODE_DELETE_GLOBAL, varname);
            deleteGlobalNode.executeWithGlobals(virtualFrame, globals);
        }
    }

    private int bytecodeDeleteSubscr(VirtualFrame virtualFrame, int stackTop, int bci, Node[] localNodes) {
        DeleteItemNode delItem = insertChildNode(localNodes, bci, DeleteItemNodeGen.class, NODE_DELETE_ITEM);
        Object slice = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        Object container = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        delItem.executeWith(virtualFrame, container, slice);
        return stackTop;
    }

    private int bytecodeStoreSubscr(VirtualFrame virtualFrame, int stackTop, int bci, Node[] localNodes, boolean useCachedNodes) {
        PyObjectSetItem setItem = insertChildNode(localNodes, bci, UNCACHED_OBJECT_SET_ITEM, PyObjectSetItemNodeGen.class, NODE_OBJECT_SET_ITEM, useCachedNodes);
        Object index = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        Object container = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        Object value = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        setItem.execute(virtualFrame, container, index, value);
        return stackTop;
    }

    private int bytecodeBuildSlice(VirtualFrame virtualFrame, int stackTop, int bci, int count, Node[] localNodes, boolean useCachedNodes) {
        Object step;
        if (count == 3) {
            step = virtualFrame.getObject(stackTop);
            virtualFrame.setObject(stackTop--, null);
        } else {
            assert count == 2;
            step = PNone.NONE;
        }
        Object stop = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        Object start = virtualFrame.getObject(stackTop);
        CreateSliceNode sliceNode = insertChildNode(localNodes, bci, UNCACHED_CREATE_SLICE, CreateSliceNodeGen.class, NODE_CREATE_SLICE, useCachedNodes);
        PSlice slice = sliceNode.execute(start, stop, step);
        virtualFrame.setObject(stackTop, slice);
        return stackTop;
    }

    private int bytecodeCallFunctionKw(VirtualFrame virtualFrame, int initialStackTop, int bci, Node[] localNodes, boolean useCachedNodes) {
        int stackTop = initialStackTop;
        CallNode callNode = insertChildNode(localNodes, bci, UNCACHED_CALL, CallNodeGen.class, NODE_CALL, useCachedNodes);
        Object callable = virtualFrame.getObject(stackTop - 2);
        Object[] args = (Object[]) virtualFrame.getObject(stackTop - 1);
        virtualFrame.setObject(stackTop - 2, callNode.execute(virtualFrame, callable, args, (PKeyword[]) virtualFrame.getObject(stackTop)));
        virtualFrame.setObject(stackTop--, null);
        virtualFrame.setObject(stackTop--, null);
        return stackTop;
    }

    private int bytecodeCallFunctionVarargs(VirtualFrame virtualFrame, int initialStackTop, int bci, Node[] localNodes, boolean useCachedNodes) {
        int stackTop = initialStackTop;
        CallNode callNode = insertChildNode(localNodes, bci, UNCACHED_CALL, CallNodeGen.class, NODE_CALL, useCachedNodes);
        Object callable = virtualFrame.getObject(stackTop - 1);
        Object[] args = (Object[]) virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop - 1, callNode.execute(virtualFrame, callable, args, PKeyword.EMPTY_KEYWORDS));
        virtualFrame.setObject(stackTop--, null);
        return stackTop;
    }

    private void bytecodeCallMethodVarargs(VirtualFrame virtualFrame, int stackTop, int bci, TruffleString[] localNames, int oparg, Node[] localNodes, boolean useCachedNodes) {
        PyObjectGetMethod getMethodNode = insertChildNode(localNodes, bci, UNCACHED_OBJECT_GET_METHOD, PyObjectGetMethodNodeGen.class, NODE_OBJECT_GET_METHOD, useCachedNodes);
        Object[] args = (Object[]) virtualFrame.getObject(stackTop);
        TruffleString methodName = localNames[oparg];
        Object rcvr = args[0];
        Object func = getMethodNode.execute(virtualFrame, rcvr, methodName);
        CallNode callNode = insertChildNode(localNodes, bci + 1, UNCACHED_CALL, CallNodeGen.class, NODE_CALL, useCachedNodes);
        virtualFrame.setObject(stackTop, callNode.execute(virtualFrame, func, args, PKeyword.EMPTY_KEYWORDS));
    }

    private int bytecodeLoadName(VirtualFrame virtualFrame, int initialStackTop, int bci, int oparg, Node[] localNodes, TruffleString[] localNames) {
        int stackTop = initialStackTop;
        ReadNameNode readNameNode = insertChildNode(localNodes, bci, ReadNameNodeGen.class, NODE_READ_NAME, localNames[oparg]);
        virtualFrame.setObject(++stackTop, readNameNode.execute(virtualFrame));
        return stackTop;
    }

    private int bytecodeCallFunction(VirtualFrame virtualFrame, int stackTop, int bci, int oparg, Node[] localNodes, boolean useCachedNodes) {
        Object func = virtualFrame.getObject(stackTop - oparg);
        switch (oparg) {
            case 0: {
                CallNode callNode = insertChildNode(localNodes, bci, UNCACHED_CALL, CallNodeGen.class, NODE_CALL, useCachedNodes);
                Object result = callNode.execute(virtualFrame, func, PythonUtils.EMPTY_OBJECT_ARRAY, PKeyword.EMPTY_KEYWORDS);
                virtualFrame.setObject(stackTop, result);
                break;
            }
            case 1: {
                CallUnaryMethodNode callNode = insertChildNode(localNodes, bci, UNCACHED_CALL_UNARY_METHOD, CallUnaryMethodNodeGen.class, NODE_CALL_UNARY_METHOD, useCachedNodes);
                Object result = callNode.executeObject(virtualFrame, func, virtualFrame.getObject(stackTop));
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop, result);
                break;
            }
            case 2: {
                CallBinaryMethodNode callNode = insertChildNode(localNodes, bci, UNCACHED_CALL_BINARY_METHOD, CallBinaryMethodNodeGen.class, NODE_CALL_BINARY_METHOD, useCachedNodes);
                Object arg1 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                Object arg0 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop, callNode.executeObject(virtualFrame, func, arg0, arg1));
                break;
            }
            case 3: {
                CallTernaryMethodNode callNode = insertChildNode(localNodes, bci, UNCACHED_CALL_TERNARY_METHOD, CallTernaryMethodNodeGen.class, NODE_CALL_TERNARY_METHOD, useCachedNodes);
                Object arg2 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                Object arg1 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                Object arg0 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop, callNode.execute(virtualFrame, func, arg0, arg1, arg2));
                break;
            }
            case 4: {
                CallQuaternaryMethodNode callNode = insertChildNode(localNodes, bci, UNCACHED_CALL_QUATERNARY_METHOD, CallQuaternaryMethodNodeGen.class, NODE_CALL_QUATERNARY_METHOD, useCachedNodes);
                Object arg3 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                Object arg2 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                Object arg1 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                Object arg0 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop, callNode.execute(virtualFrame, func, arg0, arg1, arg2, arg3));
                break;
            }
        }
        return stackTop;
    }

    private int bytecodeLoadMethod(VirtualFrame virtualFrame, int stackTop, int bci, int oparg, TruffleString[] localNames, Node[] localNodes, boolean useCachedNodes) {
        Object rcvr = virtualFrame.getObject(stackTop);
        TruffleString methodName = localNames[oparg];
        PyObjectGetMethod getMethodNode = insertChildNode(localNodes, bci, UNCACHED_OBJECT_GET_METHOD, PyObjectGetMethodNodeGen.class, NODE_OBJECT_GET_METHOD, useCachedNodes);
        Object func = getMethodNode.execute(virtualFrame, rcvr, methodName);
        virtualFrame.setObject(++stackTop, func);
        return stackTop;
    }

    private int bytecodeCallMethod(VirtualFrame virtualFrame, int stackTop, int bci, int argcount, Node[] localNodes, boolean useCachedNodes) {
        Object func = virtualFrame.getObject(stackTop - argcount);
        Object rcvr = virtualFrame.getObject(stackTop - argcount - 1);

        switch (argcount) {
            case 0: {
                CallUnaryMethodNode callNode = insertChildNode(localNodes, bci + 1, UNCACHED_CALL_UNARY_METHOD, CallUnaryMethodNodeGen.class, NODE_CALL_UNARY_METHOD, useCachedNodes);
                Object result = callNode.executeObject(virtualFrame, func, rcvr);
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop, result);
                break;
            }
            case 1: {
                CallBinaryMethodNode callNode = insertChildNode(localNodes, bci + 1, UNCACHED_CALL_BINARY_METHOD, CallBinaryMethodNodeGen.class, NODE_CALL_BINARY_METHOD, useCachedNodes);
                Object result = callNode.executeObject(virtualFrame, func, rcvr, virtualFrame.getObject(stackTop));
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop, result);
                break;
            }
            case 2: {
                CallTernaryMethodNode callNode = insertChildNode(localNodes, bci + 1, UNCACHED_CALL_TERNARY_METHOD, CallTernaryMethodNodeGen.class, NODE_CALL_TERNARY_METHOD, useCachedNodes);
                Object arg1 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                Object arg0 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop, callNode.execute(virtualFrame, func, rcvr, arg0, arg1));
                break;
            }
            case 3: {
                CallQuaternaryMethodNode callNode = insertChildNode(localNodes, bci + 1, UNCACHED_CALL_QUATERNARY_METHOD, CallQuaternaryMethodNodeGen.class, NODE_CALL_QUATERNARY_METHOD,
                                useCachedNodes);
                Object arg2 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                Object arg1 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                Object arg0 = virtualFrame.getObject(stackTop);
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop--, null);
                virtualFrame.setObject(stackTop, callNode.execute(virtualFrame, func, rcvr, arg0, arg1, arg2));
                break;
            }
        }
        return stackTop;
    }

    private int bytecodeStoreName(VirtualFrame virtualFrame, int initialStackTop, int bci, int oparg, TruffleString[] localNames, Node[] localNodes) {
        int stackTop = initialStackTop;
        Object value = virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        WriteNameNode writeNameNode = insertChildNode(localNodes, bci, WriteNameNodeGen.class, NODE_WRITE_NAME, localNames[oparg]);
        writeNameNode.execute(virtualFrame, value);
        return stackTop;
    }

    private int bytecodeRaiseVarargs(VirtualFrame virtualFrame, int stackTop, int bci, int count, Node[] localNodes) {
        RaiseNode raiseNode = insertChildNode(localNodes, bci, RaiseNodeGen.class, NODE_RAISENODE);
        Object cause;
        Object exception;
        if (count > 1) {
            cause = virtualFrame.getObject(stackTop);
            virtualFrame.setObject(stackTop--, null);
        } else {
            cause = PNone.NO_VALUE;
        }
        if (count > 0) {
            exception = virtualFrame.getObject(stackTop);
            virtualFrame.setObject(stackTop--, null);
        } else {
            exception = PNone.NO_VALUE;
        }
        raiseNode.execute(virtualFrame, exception, cause);
        return stackTop;
    }

    private void raiseUnboundCell(Node[] localNodes, int bci, int oparg, boolean useCachedNodes) {
        PRaiseNode raiseNode = insertChildNode(localNodes, bci, UNCACHED_RAISE, PRaiseNodeGen.class, NODE_RAISE, useCachedNodes);
        if (oparg < freeoffset) {
            throw raiseNode.raise(PythonBuiltinClassType.UnboundLocalError, ErrorMessages.LOCAL_VAR_REFERENCED_BEFORE_ASSIGMENT, cellvars[oparg]);
        } else {
            int varIdx = oparg - cellvars.length;
            throw raiseNode.raise(PythonBuiltinClassType.NameError, ErrorMessages.UNBOUNDFREEVAR, freevars[varIdx]);
        }
    }

    private int bytecodeImportName(VirtualFrame virtualFrame, Object globals, int initialStackTop, int bci, int oparg, TruffleString[] localNames, Node[] localNodes, boolean useCachedNodes) {
        CastToJavaIntExactNode castNode = insertChildNode(localNodes, bci, UNCACHED_CAST_TO_JAVA_INT_EXACT, CastToJavaIntExactNodeGen.class, NODE_CAST_TO_JAVA_INT_EXACT, useCachedNodes);
        TruffleString modname = localNames[oparg];
        int stackTop = initialStackTop;
        TruffleString[] fromlist = (TruffleString[]) virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        int level = castNode.execute(virtualFrame.getObject(stackTop));
        ImportNode importNode = insertChildNode(localNodes, bci + 1, ImportNode.class, NODE_IMPORT);
        Object result = importNode.execute(virtualFrame, modname, globals, fromlist, level);
        virtualFrame.setObject(stackTop, result);
        return stackTop;
    }

    private int bytecodeImportFrom(VirtualFrame virtualFrame, int initialStackTop, int bci, int oparg, TruffleString[] localNames, Node[] localNodes, boolean useCachedNodes) {
        int stackTop = initialStackTop;
        TruffleString importName = localNames[oparg];
        Object from = virtualFrame.getObject(stackTop);
        ImportFromNode importFromNode = insertChildNode(localNodes, bci, UNCACHED_IMPORT_FROM, ImportFromNodeGen.class, NODE_IMPORT_FROM, useCachedNodes);
        Object imported = importFromNode.execute(virtualFrame, from, importName);
        virtualFrame.setObject(++stackTop, imported);
        return stackTop;
    }

    private int bytecodeImportStar(VirtualFrame virtualFrame, int initialStackTop, int bci, int oparg, TruffleString[] localNames, Node[] localNodes) {
        int stackTop = initialStackTop;
        TruffleString importName = localNames[oparg];
        int level = (int) virtualFrame.getObject(stackTop);
        virtualFrame.setObject(stackTop--, null);
        ImportStarNode importStarNode = insertChildNode(localNodes, bci, ImportStarNode.class, () -> new ImportStarNode(importName, level));
        importStarNode.executeVoid(virtualFrame);
        return stackTop;
    }

    private void initCellVars(Frame localFrame) {
        if (cellvars.length <= 32) {
            initCellVarsExploded(localFrame);
        } else {
            initCellVarsLoop(localFrame);
        }
    }

    @ExplodeLoop
    private void initCellVarsExploded(Frame localFrame) {
        for (int i = 0; i < cellvars.length; i++) {
            initCell(localFrame, i);
        }
    }

    private void initCellVarsLoop(Frame localFrame) {
        for (int i = 0; i < cellvars.length; i++) {
            initCell(localFrame, i);
        }
    }

    private void initCell(Frame localFrame, int i) {
        PCell cell = new PCell(cellEffectivelyFinalAssumptions[i]);
        localFrame.setObject(celloffset + i, cell);
        if (cell2arg != null && cell2arg[i] != -1) {
            int idx = cell2arg[i];
            cell.setRef(localFrame.getObject(idx));
            localFrame.setObject(idx, null);
        }
    }

    private void initFreeVars(Frame localFrame, Object[] originalArgs) {
        if (freevars.length > 0) {
            if (freevars.length <= 32) {
                initFreeVarsExploded(localFrame, originalArgs);
            } else {
                initFreeVarsLoop(localFrame, originalArgs);
            }
        }
    }

    @ExplodeLoop
    private void initFreeVarsExploded(Frame localFrame, Object[] originalArgs) {
        PCell[] closure = PArguments.getClosure(originalArgs);
        for (int i = 0; i < freevars.length; i++) {
            localFrame.setObject(freeoffset + i, closure[i]);
        }
    }

    private void initFreeVarsLoop(Frame localFrame, Object[] originalArgs) {
        PCell[] closure = PArguments.getClosure(originalArgs);
        for (int i = 0; i < freevars.length; i++) {
            localFrame.setObject(freeoffset + i, closure[i]);
        }
    }

    @ExplodeLoop
    @SuppressWarnings("unchecked")
    private static <T> void moveFromStack(VirtualFrame virtualFrame, int start, int stop, T[] target) {
        CompilerAsserts.partialEvaluationConstant(start);
        CompilerAsserts.partialEvaluationConstant(stop);
        for (int j = 0, i = start; i < stop; i++, j++) {
            target[j] = (T) virtualFrame.getObject(i);
            virtualFrame.setObject(i, null);
        }
    }

    private int bytecodeCollectionFromStack(VirtualFrame virtualFrame, int type, int count, int oldStackTop, Node[] localNodes, int nodeIndex, boolean useCachedNodes) {
        int stackTop = oldStackTop;
        Object res = null;
        switch (type) {
            case CollectionBits.LIST: {
                Object[] store = new Object[count];
                moveFromStack(virtualFrame, stackTop - count + 1, stackTop + 1, store);
                res = factory.createList(store);
                break;
            }
            case CollectionBits.TUPLE: {
                Object[] store = new Object[count];
                moveFromStack(virtualFrame, stackTop - count + 1, stackTop + 1, store);
                res = factory.createTuple(store);
                break;
            }
            case CollectionBits.SET: {
                PSet set = factory.createSet();
                HashingCollectionNodes.SetItemNode newNode = insertChildNode(localNodes, nodeIndex, UNCACHED_SET_ITEM, HashingCollectionNodesFactory.SetItemNodeGen.class, NODE_SET_ITEM,
                                useCachedNodes);
                for (int i = stackTop - count + 1; i <= stackTop; i++) {
                    newNode.execute(virtualFrame, set, virtualFrame.getObject(i), PNone.NONE);
                    virtualFrame.setObject(i, null);
                }
                res = set;
                break;
            }
            case CollectionBits.DICT: {
                PDict dict = factory.createDict();
                HashingCollectionNodes.SetItemNode setItem = insertChildNode(localNodes, nodeIndex, UNCACHED_SET_ITEM, HashingCollectionNodesFactory.SetItemNodeGen.class, NODE_SET_ITEM,
                                useCachedNodes);
                assert count % 2 == 0;
                for (int i = stackTop - count + 1; i <= stackTop; i += 2) {
                    setItem.execute(virtualFrame, dict, virtualFrame.getObject(i), virtualFrame.getObject(i + 1));
                    virtualFrame.setObject(i, null);
                    virtualFrame.setObject(i + 1, null);
                }
                res = dict;
                break;
            }
            case CollectionBits.KWORDS: {
                PKeyword[] kwds = new PKeyword[count];
                moveFromStack(virtualFrame, stackTop - count + 1, stackTop + 1, kwds);
                res = kwds;
                break;
            }
            case CollectionBits.OBJECT: {
                Object[] objs = new Object[count];
                moveFromStack(virtualFrame, stackTop - count + 1, stackTop + 1, objs);
                res = objs;
                break;
            }
        }
        stackTop -= count;
        virtualFrame.setObject(++stackTop, res);
        return stackTop;
    }

    private void bytecodeCollectionFromCollection(VirtualFrame virtualFrame, int type, int stackTop, Node[] localNodes, int nodeIndex, boolean useCachedNodes) {
        Object sourceCollection = virtualFrame.getObject(stackTop);
        Object result;
        switch (type) {
            case CollectionBits.LIST: {
                ListNodes.ConstructListNode constructNode = insertChildNode(localNodes, nodeIndex, UNCACHED_CONSTRUCT_LIST, ListNodesFactory.ConstructListNodeGen.class, NODE_CONSTRUCT_LIST,
                                useCachedNodes);
                result = constructNode.execute(virtualFrame, sourceCollection);
                break;
            }
            case CollectionBits.TUPLE: {
                TupleNodes.ConstructTupleNode constructNode = insertChildNode(localNodes, nodeIndex, UNCACHED_CONSTRUCT_TUPLE, TupleNodesFactory.ConstructTupleNodeGen.class, NODE_CONSTRUCT_TUPLE,
                                useCachedNodes);
                result = constructNode.execute(virtualFrame, sourceCollection);
                break;
            }
            case CollectionBits.SET: {
                SetNodes.ConstructSetNode constructNode = insertChildNode(localNodes, nodeIndex, UNCACHED_CONSTRUCT_SET, SetNodesFactory.ConstructSetNodeGen.class, NODE_CONSTRUCT_SET, useCachedNodes);
                result = constructNode.executeWith(virtualFrame, sourceCollection);
                break;
            }
            case CollectionBits.DICT: {
                // TODO create uncached node
                HashingStorage.InitNode initNode = insertChildNode(localNodes, nodeIndex, HashingStorageFactory.InitNodeGen.class, NODE_HASHING_STORAGE_INIT);
                HashingStorage storage = initNode.execute(virtualFrame, sourceCollection, PKeyword.EMPTY_KEYWORDS);
                result = factory.createDict(storage);
                break;
            }
            case CollectionBits.OBJECT: {
                ExecutePositionalStarargsNode executeStarargsNode = insertChildNode(localNodes, nodeIndex, UNCACHED_EXECUTE_STARARGS, ExecutePositionalStarargsNodeGen.class, NODE_EXECUTE_STARARGS,
                                useCachedNodes);
                result = executeStarargsNode.executeWith(virtualFrame, sourceCollection);
                break;
            }
            case CollectionBits.KWORDS: {
                KeywordsNode keywordsNode = insertChildNode(localNodes, nodeIndex, UNCACHED_KEYWORDS, KeywordsNodeGen.class, NODE_KEYWORDS, useCachedNodes);
                result = keywordsNode.execute(virtualFrame, sourceCollection, stackTop);
                break;
            }
            default:
                throw CompilerDirectives.shouldNotReachHere("Unexpected collection type");
        }
        virtualFrame.setObject(stackTop, result);
    }

    private int bytecodeCollectionAddCollection(VirtualFrame virtualFrame, int type, int initialStackTop, Node[] localNodes, int nodeIndex, boolean useCachedNodes) {
        int stackTop = initialStackTop;
        Object collection1 = virtualFrame.getObject(stackTop - 1);
        Object collection2 = virtualFrame.getObject(stackTop);
        Object result;
        switch (type) {
            case CollectionBits.LIST: {
                // TODO uncached node
                ListBuiltins.ListExtendNode extendNode = insertChildNode(localNodes, nodeIndex, ListBuiltinsFactory.ListExtendNodeFactory.ListExtendNodeGen.class, NODE_LIST_EXTEND);
                extendNode.execute(virtualFrame, (PList) collection1, collection2);
                result = collection1;
                break;
            }
            case CollectionBits.SET: {
                SetBuiltins.UpdateSingleNode updateNode = insertChildNode(localNodes, nodeIndex, UNCACHED_SET_UPDATE, SetBuiltinsFactory.UpdateSingleNodeGen.class, NODE_SET_UPDATE, useCachedNodes);
                PSet set = (PSet) collection1;
                set.setDictStorage(updateNode.execute(virtualFrame, set.getDictStorage(), collection2));
                result = set;
                break;
            }
            case CollectionBits.DICT: {
                // TODO uncached node
                DictNodes.UpdateNode updateNode = insertChildNode(localNodes, nodeIndex, DictNodesFactory.UpdateNodeGen.class, NODE_DICT_UPDATE);
                updateNode.execute(virtualFrame, (PDict) collection1, collection2);
                result = collection1;
                break;
            }
            // Note: we don't allow this operation for tuple
            case CollectionBits.OBJECT: {
                Object[] array1 = (Object[]) collection1;
                ExecutePositionalStarargsNode executeStarargsNode = insertChildNode(localNodes, nodeIndex, UNCACHED_EXECUTE_STARARGS, ExecutePositionalStarargsNodeGen.class, NODE_EXECUTE_STARARGS,
                                useCachedNodes);
                Object[] array2 = executeStarargsNode.executeWith(virtualFrame, collection2);
                Object[] combined = new Object[array1.length + array2.length];
                System.arraycopy(array1, 0, combined, 0, array1.length);
                System.arraycopy(array2, 0, combined, array1.length, array2.length);
                result = combined;
                break;
            }
            case CollectionBits.KWORDS: {
                PKeyword[] array1 = (PKeyword[]) collection1;
                PKeyword[] array2 = (PKeyword[]) collection2;
                PKeyword[] combined = new PKeyword[array1.length + array2.length];
                System.arraycopy(array1, 0, combined, 0, array1.length);
                System.arraycopy(array2, 0, combined, array1.length, array2.length);
                result = combined;
                break;
            }
            default:
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw PRaiseNode.getUncached().raise(SystemError, ErrorMessages.INVALID_TYPE_FOR_S, "COLLECTION_ADD_COLLECTION");
        }
        virtualFrame.setObject(stackTop--, null);
        virtualFrame.setObject(stackTop, result);
        return stackTop;
    }

    private int bytecodeAddToCollection(VirtualFrame virtualFrame, int initialStackTop, int nodeIndex, Node[] localNodes, int depth, int type, boolean useCachedNodes) {
        int stackTop = initialStackTop;
        Object collection = virtualFrame.getObject(stackTop - depth);
        Object item = virtualFrame.getObject(stackTop);
        switch (type) {
            case CollectionBits.LIST: {
                ListNodes.AppendNode appendNode = insertChildNode(localNodes, nodeIndex, UNCACHED_LIST_APPEND, ListNodesFactory.AppendNodeGen.class, NODE_LIST_APPEND, useCachedNodes);
                appendNode.execute((PList) collection, item);
                break;
            }
            case CollectionBits.SET: {
                SetNodes.AddNode addNode = insertChildNode(localNodes, nodeIndex, UNCACHED_SET_ADD, SetNodesFactory.AddNodeGen.class, NODE_SET_ADD, useCachedNodes);
                addNode.execute(virtualFrame, (PSet) collection, item);
                break;
            }
            case CollectionBits.DICT: {
                Object key = virtualFrame.getObject(stackTop - 1);
                HashingCollectionNodes.SetItemNode setItem = insertChildNode(localNodes, nodeIndex, UNCACHED_SET_ITEM, HashingCollectionNodesFactory.SetItemNodeGen.class, NODE_SET_ITEM,
                                useCachedNodes);
                setItem.execute(virtualFrame, (PDict) collection, key, item);
                virtualFrame.setObject(stackTop--, null);
                break;
            }
            default:
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw PRaiseNode.getUncached().raise(SystemError, ErrorMessages.INVALID_TYPE_FOR_S, "ADD_TO_COLLECTION");
        }
        virtualFrame.setObject(stackTop--, null);
        return stackTop;
    }

    private int bytecodeUnpackSequence(VirtualFrame virtualFrame, int stackTop, int bci, Node[] localNodes, int count, boolean useCachedNodes) {
        UnpackSequenceNode unpackNode = insertChildNode(localNodes, bci, UNCACHED_UNPACK_SEQUENCE, UnpackSequenceNodeGen.class, NODE_UNPACK_SEQUENCE, useCachedNodes);
        Object collection = virtualFrame.getObject(stackTop);
        return unpackNode.execute(virtualFrame, stackTop - 1, collection, count);
    }

    private int bytecodeUnpackEx(VirtualFrame virtualFrame, int stackTop, int bci, Node[] localNodes, int countBefore, int countAfter, boolean useCachedNodes) {
        UnpackExNode unpackNode = insertChildNode(localNodes, bci, UNCACHED_UNPACK_EX, UnpackExNodeGen.class, NODE_UNPACK_EX, useCachedNodes);
        Object collection = virtualFrame.getObject(stackTop);
        return unpackNode.execute(virtualFrame, stackTop - 1, collection, countBefore, countAfter);
    }

    @ExplodeLoop
    private int findHandler(int bci) {
        CompilerAsserts.partialEvaluationConstant(bci);

        for (int i = 0; i < exceptionHandlerRanges.length; i += 4) {
            // The ranges are ordered by their start and non-overlapping
            if (bci < exceptionHandlerRanges[i]) {
                break;
            } else if (bci < exceptionHandlerRanges[i + 1]) {
                // bci is inside this try-block range. get the target stack size
                return i + 2;
            }
        }
        return -1;
    }

    @ExplodeLoop
    private static int unwindBlock(VirtualFrame virtualFrame, int stackTop, int stackTopBeforeBlock) {
        CompilerAsserts.partialEvaluationConstant(stackTop);
        CompilerAsserts.partialEvaluationConstant(stackTopBeforeBlock);
        for (int i = stackTop; i > stackTopBeforeBlock; i--) {
            virtualFrame.setObject(i, null);
        }
        return stackTopBeforeBlock;
    }

    public PCell readClassCell(VirtualFrame virtualFrame) {
        Frame localFrame = virtualFrame;
        if (co.isGeneratorOrCoroutine()) {
            localFrame = PArguments.getGeneratorFrame(virtualFrame);
        }
        if (classcellIndex < 0) {
            return null;
        }
        return (PCell) localFrame.getObject(classcellIndex);
    }

    public Object readSelf(VirtualFrame virtualFrame) {
        Frame localFrame = virtualFrame;
        if (co.isGeneratorOrCoroutine()) {
            localFrame = PArguments.getGeneratorFrame(virtualFrame);
        }
        if (selfIndex < 0) {
            return null;
        } else if (selfIndex == 0) {
            return localFrame.getObject(0);
        } else {
            PCell selfCell = (PCell) localFrame.getObject(selfIndex);
            return selfCell.getRef();
        }
    }

    public int getStartOffset() {
        return co.startOffset;
    }

    @TruffleBoundary
    public int bciToLine(int bci) {
        if (source != null && source.hasCharacters() && bci >= 0) {
            /*
             * TODO We only store source offsets, which makes it impossible to reconstruct linenos
             * without the text source. We should store lines and columns separately like CPython.
             */
            return source.createSection(co.bciToSrcOffset(bci), 0).getStartLine();
        }
        return -1;
    }

    @TruffleBoundary
    public int getFirstLineno() {
        if (source != null && source.hasCharacters()) {
            // TODO the same problem as bciToLine
            return source.createSection(co.startOffset, 0).getStartLine();
        }
        return -1;
    }

    @Override
    public SourceSection getSourceSection() {
        if (sourceSection != null) {
            return sourceSection;
        } else if (source == null) {
            return null;
        } else if (!source.hasCharacters()) {
            /*
             * TODO We could still expose the disassembled bytecode for a debugger to have something
             * to step through.
             */
            sourceSection = source.createUnavailableSection();
            return sourceSection;
        } else {
            sourceSection = source.createSection(co.startOffset, co.findMaxOffset() - co.startOffset);
            return sourceSection;
        }
    }

    @Override
    protected byte[] extractCode() {
        /*
         * CPython exposes individual items of code objects, like constants, as different members of
         * the code object and the co_code attribute contains just the bytecode. It would be better
         * if we did the same, however we currently serialize everything into just co_code and
         * ignore the rest. The reasons are:
         *
         * 1) TruffleLanguage.parsePublic does source level caching but it only accepts bytes or
         * Strings. We could cache ourselves instead, but we have to come up with a cache key. It
         * would be impractical to compute a cache key from all the deserialized constants, but we
         * could just generate a large random number at compile time to serve as a key.
         *
         * 2) The arguments of code object constructor would be different. Some libraries like
         * cloudpickle (used by pyspark) still rely on particular signature, even though CPython has
         * changed theirs several times. We would have to match CPython's signature. It's doable,
         * but it would certainly be more practical to update to 3.11 first to have an attribute for
         * exception ranges.
         *
         * 3) While the AST interpreter is still in use, we have to share the code in CodeBuiltins,
         * so it's much simpler to do it in a way that is close to what the AST interpreter is
         * doing.
         *
         * TODO We should revisit this when the AST interpreter is removed.
         */
        return MarshalModuleBuiltins.serializeCodeUnit(co);
    }

    @Override
    protected boolean isCloneUninitializedSupported() {
        return true;
    }

    @Override
    protected RootNode cloneUninitialized() {
        return new PBytecodeRootNode(PythonLanguage.get(this), getFrameDescriptor(), getSignature(), co, source);
    }
}