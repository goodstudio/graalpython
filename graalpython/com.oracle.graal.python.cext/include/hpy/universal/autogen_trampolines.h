
/*
   DO NOT EDIT THIS FILE!

   This file is automatically generated by hpy.tools.autogen.trampolines.autogen_trampolines_h
   See also hpy.tools.autogen and hpy/tools/public_api.h

   Run this to regenerate:
       make autogen

*/

HPyAPI_FUNC HPy HPyModule_Create(HPyContext *ctx, HPyModuleDef *def) {
     return ctx->ctx_Module_Create ( ctx, def ); 
}

HPyAPI_FUNC HPy HPy_Dup(HPyContext *ctx, HPy h) {
     return ctx->ctx_Dup ( ctx, h ); 
}

HPyAPI_FUNC void HPy_Close(HPyContext *ctx, HPy h) {
     ctx->ctx_Close ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyLong_FromLong(HPyContext *ctx, long value) {
     return ctx->ctx_Long_FromLong ( ctx, value ); 
}

HPyAPI_FUNC HPy HPyLong_FromUnsignedLong(HPyContext *ctx, unsigned long value) {
     return ctx->ctx_Long_FromUnsignedLong ( ctx, value ); 
}

HPyAPI_FUNC HPy HPyLong_FromLongLong(HPyContext *ctx, long long v) {
     return ctx->ctx_Long_FromLongLong ( ctx, v ); 
}

HPyAPI_FUNC HPy HPyLong_FromUnsignedLongLong(HPyContext *ctx, unsigned long long v) {
     return ctx->ctx_Long_FromUnsignedLongLong ( ctx, v ); 
}

HPyAPI_FUNC HPy HPyLong_FromSize_t(HPyContext *ctx, size_t value) {
     return ctx->ctx_Long_FromSize_t ( ctx, value ); 
}

HPyAPI_FUNC HPy HPyLong_FromSsize_t(HPyContext *ctx, HPy_ssize_t value) {
     return ctx->ctx_Long_FromSsize_t ( ctx, value ); 
}

HPyAPI_FUNC long HPyLong_AsLong(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsLong ( ctx, h ); 
}

HPyAPI_FUNC unsigned long HPyLong_AsUnsignedLong(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsUnsignedLong ( ctx, h ); 
}

HPyAPI_FUNC unsigned long HPyLong_AsUnsignedLongMask(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsUnsignedLongMask ( ctx, h ); 
}

HPyAPI_FUNC long long HPyLong_AsLongLong(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsLongLong ( ctx, h ); 
}

HPyAPI_FUNC unsigned long long HPyLong_AsUnsignedLongLong(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsUnsignedLongLong ( ctx, h ); 
}

HPyAPI_FUNC unsigned long long HPyLong_AsUnsignedLongLongMask(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsUnsignedLongLongMask ( ctx, h ); 
}

HPyAPI_FUNC size_t HPyLong_AsSize_t(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsSize_t ( ctx, h ); 
}

HPyAPI_FUNC HPy_ssize_t HPyLong_AsSsize_t(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsSsize_t ( ctx, h ); 
}

HPyAPI_FUNC void *HPyLong_AsVoidPtr(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsVoidPtr ( ctx, h ); 
}

HPyAPI_FUNC double HPyLong_AsDouble(HPyContext *ctx, HPy h) {
     return ctx->ctx_Long_AsDouble ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyFloat_FromDouble(HPyContext *ctx, double v) {
     return ctx->ctx_Float_FromDouble ( ctx, v ); 
}

HPyAPI_FUNC double HPyFloat_AsDouble(HPyContext *ctx, HPy h) {
     return ctx->ctx_Float_AsDouble ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyBool_FromLong(HPyContext *ctx, long v) {
     return ctx->ctx_Bool_FromLong ( ctx, v ); 
}

HPyAPI_FUNC HPy_ssize_t HPy_Length(HPyContext *ctx, HPy h) {
     return ctx->ctx_Length ( ctx, h ); 
}

HPyAPI_FUNC int HPySequence_Check(HPyContext *ctx, HPy h) {
     return ctx->ctx_Sequence_Check ( ctx, h ); 
}

HPyAPI_FUNC int HPyNumber_Check(HPyContext *ctx, HPy h) {
     return ctx->ctx_Number_Check ( ctx, h ); 
}

HPyAPI_FUNC HPy HPy_Add(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_Add ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_Subtract(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_Subtract ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_Multiply(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_Multiply ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_MatrixMultiply(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_MatrixMultiply ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_FloorDivide(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_FloorDivide ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_TrueDivide(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_TrueDivide ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_Remainder(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_Remainder ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_Divmod(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_Divmod ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_Power(HPyContext *ctx, HPy h1, HPy h2, HPy h3) {
     return ctx->ctx_Power ( ctx, h1, h2, h3 ); 
}

HPyAPI_FUNC HPy HPy_Negative(HPyContext *ctx, HPy h1) {
     return ctx->ctx_Negative ( ctx, h1 ); 
}

HPyAPI_FUNC HPy HPy_Positive(HPyContext *ctx, HPy h1) {
     return ctx->ctx_Positive ( ctx, h1 ); 
}

HPyAPI_FUNC HPy HPy_Absolute(HPyContext *ctx, HPy h1) {
     return ctx->ctx_Absolute ( ctx, h1 ); 
}

HPyAPI_FUNC HPy HPy_Invert(HPyContext *ctx, HPy h1) {
     return ctx->ctx_Invert ( ctx, h1 ); 
}

HPyAPI_FUNC HPy HPy_Lshift(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_Lshift ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_Rshift(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_Rshift ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_And(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_And ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_Xor(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_Xor ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_Or(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_Or ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_Index(HPyContext *ctx, HPy h1) {
     return ctx->ctx_Index ( ctx, h1 ); 
}

HPyAPI_FUNC HPy HPy_Long(HPyContext *ctx, HPy h1) {
     return ctx->ctx_Long ( ctx, h1 ); 
}

HPyAPI_FUNC HPy HPy_Float(HPyContext *ctx, HPy h1) {
     return ctx->ctx_Float ( ctx, h1 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceAdd(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceAdd ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceSubtract(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceSubtract ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceMultiply(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceMultiply ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceMatrixMultiply(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceMatrixMultiply ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceFloorDivide(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceFloorDivide ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceTrueDivide(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceTrueDivide ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceRemainder(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceRemainder ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlacePower(HPyContext *ctx, HPy h1, HPy h2, HPy h3) {
     return ctx->ctx_InPlacePower ( ctx, h1, h2, h3 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceLshift(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceLshift ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceRshift(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceRshift ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceAnd(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceAnd ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceXor(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceXor ( ctx, h1, h2 ); 
}

HPyAPI_FUNC HPy HPy_InPlaceOr(HPyContext *ctx, HPy h1, HPy h2) {
     return ctx->ctx_InPlaceOr ( ctx, h1, h2 ); 
}

HPyAPI_FUNC int HPyCallable_Check(HPyContext *ctx, HPy h) {
     return ctx->ctx_Callable_Check ( ctx, h ); 
}

HPyAPI_FUNC HPy HPy_CallTupleDict(HPyContext *ctx, HPy callable, HPy args, HPy kw) {
     return ctx->ctx_CallTupleDict ( ctx, callable, args, kw ); 
}

HPyAPI_FUNC HPy HPyErr_SetString(HPyContext *ctx, HPy h_type, const char *message) {
     ctx->ctx_Err_SetString ( ctx, h_type, message ); return HPy_NULL; 
}

HPyAPI_FUNC HPy HPyErr_SetObject(HPyContext *ctx, HPy h_type, HPy h_value) {
     ctx->ctx_Err_SetObject ( ctx, h_type, h_value ); return HPy_NULL; 
}

HPyAPI_FUNC HPy HPyErr_SetFromErrnoWithFilename(HPyContext *ctx, HPy h_type, const char *filename_fsencoded) {
     return ctx->ctx_Err_SetFromErrnoWithFilename ( ctx, h_type, filename_fsencoded ); 
}

HPyAPI_FUNC HPy HPyErr_SetFromErrnoWithFilenameObjects(HPyContext *ctx, HPy h_type, HPy filename1, HPy filename2) {
     ctx->ctx_Err_SetFromErrnoWithFilenameObjects ( ctx, h_type, filename1, filename2 ); return HPy_NULL; 
}

HPyAPI_FUNC int HPyErr_Occurred(HPyContext *ctx) {
     return ctx->ctx_Err_Occurred ( ctx ); 
}

HPyAPI_FUNC int HPyErr_ExceptionMatches(HPyContext *ctx, HPy exc) {
     return ctx->ctx_Err_ExceptionMatches ( ctx, exc ); 
}

HPyAPI_FUNC HPy HPyErr_NoMemory(HPyContext *ctx) {
     ctx->ctx_Err_NoMemory ( ctx ); return HPy_NULL; 
}

HPyAPI_FUNC void HPyErr_Clear(HPyContext *ctx) {
     ctx->ctx_Err_Clear ( ctx ); 
}

HPyAPI_FUNC HPy HPyErr_NewException(HPyContext *ctx, const char *name, HPy base, HPy dict) {
     return ctx->ctx_Err_NewException ( ctx, name, base, dict ); 
}

HPyAPI_FUNC HPy HPyErr_NewExceptionWithDoc(HPyContext *ctx, const char *name, const char *doc, HPy base, HPy dict) {
     return ctx->ctx_Err_NewExceptionWithDoc ( ctx, name, doc, base, dict ); 
}

HPyAPI_FUNC int HPyErr_WarnEx(HPyContext *ctx, HPy category, const char *message, HPy_ssize_t stack_level) {
     return ctx->ctx_Err_WarnEx ( ctx, category, message, stack_level ); 
}

HPyAPI_FUNC void HPyErr_WriteUnraisable(HPyContext *ctx, HPy obj) {
     ctx->ctx_Err_WriteUnraisable ( ctx, obj ); 
}

HPyAPI_FUNC int HPy_IsTrue(HPyContext *ctx, HPy h) {
     return ctx->ctx_IsTrue ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyType_FromSpec(HPyContext *ctx, HPyType_Spec *spec, HPyType_SpecParam *params) {
     return ctx->ctx_Type_FromSpec ( ctx, spec, params ); 
}

HPyAPI_FUNC HPy HPyType_GenericNew(HPyContext *ctx, HPy type, HPy *args, HPy_ssize_t nargs, HPy kw) {
     return ctx->ctx_Type_GenericNew ( ctx, type, args, nargs, kw ); 
}

HPyAPI_FUNC HPy HPy_GetAttr(HPyContext *ctx, HPy obj, HPy name) {
     return ctx->ctx_GetAttr ( ctx, obj, name ); 
}

HPyAPI_FUNC HPy HPy_GetAttr_s(HPyContext *ctx, HPy obj, const char *name) {
     return ctx->ctx_GetAttr_s ( ctx, obj, name ); 
}

HPyAPI_FUNC HPy HPy_MaybeGetAttr_s(HPyContext *ctx, HPy obj, const char *name) {
     return ctx->ctx_MaybeGetAttr_s ( ctx, obj, name ); 
}

HPyAPI_FUNC int HPy_HasAttr(HPyContext *ctx, HPy obj, HPy name) {
     return ctx->ctx_HasAttr ( ctx, obj, name ); 
}

HPyAPI_FUNC int HPy_HasAttr_s(HPyContext *ctx, HPy obj, const char *name) {
     return ctx->ctx_HasAttr_s ( ctx, obj, name ); 
}

HPyAPI_FUNC int HPy_SetAttr(HPyContext *ctx, HPy obj, HPy name, HPy value) {
     return ctx->ctx_SetAttr ( ctx, obj, name, value ); 
}

HPyAPI_FUNC int HPy_SetAttr_s(HPyContext *ctx, HPy obj, const char *name, HPy value) {
     return ctx->ctx_SetAttr_s ( ctx, obj, name, value ); 
}

HPyAPI_FUNC HPy HPy_GetItem(HPyContext *ctx, HPy obj, HPy key) {
     return ctx->ctx_GetItem ( ctx, obj, key ); 
}

HPyAPI_FUNC HPy HPy_GetItem_i(HPyContext *ctx, HPy obj, HPy_ssize_t idx) {
     return ctx->ctx_GetItem_i ( ctx, obj, idx ); 
}

HPyAPI_FUNC HPy HPy_GetItem_s(HPyContext *ctx, HPy obj, const char *key) {
     return ctx->ctx_GetItem_s ( ctx, obj, key ); 
}

HPyAPI_FUNC int HPy_Contains(HPyContext *ctx, HPy container, HPy key) {
     return ctx->ctx_Contains ( ctx, container, key ); 
}

HPyAPI_FUNC int HPy_SetItem(HPyContext *ctx, HPy obj, HPy key, HPy value) {
     return ctx->ctx_SetItem ( ctx, obj, key, value ); 
}

HPyAPI_FUNC int HPy_SetItem_i(HPyContext *ctx, HPy obj, HPy_ssize_t idx, HPy value) {
     return ctx->ctx_SetItem_i ( ctx, obj, idx, value ); 
}

HPyAPI_FUNC int HPy_SetItem_s(HPyContext *ctx, HPy obj, const char *key, HPy value) {
     return ctx->ctx_SetItem_s ( ctx, obj, key, value ); 
}

HPyAPI_FUNC HPy HPy_Type(HPyContext *ctx, HPy obj) {
     return ctx->ctx_Type ( ctx, obj ); 
}

HPyAPI_FUNC int HPy_TypeCheck(HPyContext *ctx, HPy obj, HPy type) {
     return ctx->ctx_TypeCheck ( ctx, obj, type ); 
}

HPyAPI_FUNC int HPy_SetType(HPyContext *ctx, HPy obj, HPy type) {
     return ctx->ctx_SetType ( ctx, obj, type ); 
}

HPyAPI_FUNC int HPyType_IsSubtype(HPyContext *ctx, HPy sub, HPy type) {
     return ctx->ctx_Type_IsSubtype ( ctx, sub, type ); 
}

HPyAPI_FUNC const char *HPyType_GetName(HPyContext *ctx, HPy type) {
     return ctx->ctx_Type_GetName ( ctx, type ); 
}

HPyAPI_FUNC int HPy_Is(HPyContext *ctx, HPy obj, HPy other) {
     return ctx->ctx_Is ( ctx, obj, other ); 
}

HPyAPI_FUNC void *HPy_AsStruct(HPyContext *ctx, HPy h) {
     return ctx->ctx_AsStruct ( ctx, h ); 
}

HPyAPI_FUNC void *HPy_AsStructLegacy(HPyContext *ctx, HPy h) {
     return ctx->ctx_AsStructLegacy ( ctx, h ); 
}

HPyAPI_FUNC HPy HPy_Repr(HPyContext *ctx, HPy obj) {
     return ctx->ctx_Repr ( ctx, obj ); 
}

HPyAPI_FUNC HPy HPy_Str(HPyContext *ctx, HPy obj) {
     return ctx->ctx_Str ( ctx, obj ); 
}

HPyAPI_FUNC HPy HPy_ASCII(HPyContext *ctx, HPy obj) {
     return ctx->ctx_ASCII ( ctx, obj ); 
}

HPyAPI_FUNC HPy HPy_Bytes(HPyContext *ctx, HPy obj) {
     return ctx->ctx_Bytes ( ctx, obj ); 
}

HPyAPI_FUNC HPy HPy_RichCompare(HPyContext *ctx, HPy v, HPy w, int op) {
     return ctx->ctx_RichCompare ( ctx, v, w, op ); 
}

HPyAPI_FUNC int HPy_RichCompareBool(HPyContext *ctx, HPy v, HPy w, int op) {
     return ctx->ctx_RichCompareBool ( ctx, v, w, op ); 
}

HPyAPI_FUNC HPy_hash_t HPy_Hash(HPyContext *ctx, HPy obj) {
     return ctx->ctx_Hash ( ctx, obj ); 
}

HPyAPI_FUNC HPy HPySeqIter_New(HPyContext *ctx, HPy seq) {
     return ctx->ctx_SeqIter_New ( ctx, seq ); 
}

HPyAPI_FUNC int HPyBytes_Check(HPyContext *ctx, HPy h) {
     return ctx->ctx_Bytes_Check ( ctx, h ); 
}

HPyAPI_FUNC HPy_ssize_t HPyBytes_Size(HPyContext *ctx, HPy h) {
     return ctx->ctx_Bytes_Size ( ctx, h ); 
}

HPyAPI_FUNC HPy_ssize_t HPyBytes_GET_SIZE(HPyContext *ctx, HPy h) {
     return ctx->ctx_Bytes_GET_SIZE ( ctx, h ); 
}

HPyAPI_FUNC char *HPyBytes_AsString(HPyContext *ctx, HPy h) {
     return ctx->ctx_Bytes_AsString ( ctx, h ); 
}

HPyAPI_FUNC char *HPyBytes_AS_STRING(HPyContext *ctx, HPy h) {
     return ctx->ctx_Bytes_AS_STRING ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyBytes_FromString(HPyContext *ctx, const char *v) {
     return ctx->ctx_Bytes_FromString ( ctx, v ); 
}

HPyAPI_FUNC HPy HPyBytes_FromStringAndSize(HPyContext *ctx, const char *v, HPy_ssize_t len) {
     return ctx->ctx_Bytes_FromStringAndSize ( ctx, v, len ); 
}

HPyAPI_FUNC HPy HPyUnicode_FromString(HPyContext *ctx, const char *utf8) {
     return ctx->ctx_Unicode_FromString ( ctx, utf8 ); 
}

HPyAPI_FUNC int HPyUnicode_Check(HPyContext *ctx, HPy h) {
     return ctx->ctx_Unicode_Check ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyUnicode_AsASCIIString(HPyContext *ctx, HPy h) {
     return ctx->ctx_Unicode_AsASCIIString ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyUnicode_AsLatin1String(HPyContext *ctx, HPy h) {
     return ctx->ctx_Unicode_AsLatin1String ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyUnicode_AsUTF8String(HPyContext *ctx, HPy h) {
     return ctx->ctx_Unicode_AsUTF8String ( ctx, h ); 
}

HPyAPI_FUNC const char *HPyUnicode_AsUTF8AndSize(HPyContext *ctx, HPy h, HPy_ssize_t *size) {
     return ctx->ctx_Unicode_AsUTF8AndSize ( ctx, h, size ); 
}

HPyAPI_FUNC HPy HPyUnicode_FromWideChar(HPyContext *ctx, const wchar_t *w, HPy_ssize_t size) {
     return ctx->ctx_Unicode_FromWideChar ( ctx, w, size ); 
}

HPyAPI_FUNC HPy HPyUnicode_DecodeFSDefault(HPyContext *ctx, const char *v) {
     return ctx->ctx_Unicode_DecodeFSDefault ( ctx, v ); 
}

HPyAPI_FUNC HPy HPyUnicode_DecodeFSDefaultAndSize(HPyContext *ctx, const char *v, HPy_ssize_t size) {
     return ctx->ctx_Unicode_DecodeFSDefaultAndSize ( ctx, v, size ); 
}

HPyAPI_FUNC HPy HPyUnicode_EncodeFSDefault(HPyContext *ctx, HPy h) {
     return ctx->ctx_Unicode_EncodeFSDefault ( ctx, h ); 
}

HPyAPI_FUNC HPy_UCS4 HPyUnicode_ReadChar(HPyContext *ctx, HPy h, HPy_ssize_t index) {
     return ctx->ctx_Unicode_ReadChar ( ctx, h, index ); 
}

HPyAPI_FUNC HPy HPyUnicode_DecodeASCII(HPyContext *ctx, const char *s, HPy_ssize_t size, const char *errors) {
     return ctx->ctx_Unicode_DecodeASCII ( ctx, s, size, errors ); 
}

HPyAPI_FUNC HPy HPyUnicode_DecodeLatin1(HPyContext *ctx, const char *s, HPy_ssize_t size, const char *errors) {
     return ctx->ctx_Unicode_DecodeLatin1 ( ctx, s, size, errors ); 
}

HPyAPI_FUNC HPy HPyUnicode_FromEncodedObject(HPyContext *ctx, HPy obj, const char *encoding, const char *errors) {
     return ctx->ctx_Unicode_FromEncodedObject ( ctx, obj, encoding, errors ); 
}

HPyAPI_FUNC HPy HPyUnicode_InternFromString(HPyContext *ctx, const char *str) {
     return ctx->ctx_Unicode_InternFromString ( ctx, str ); 
}

HPyAPI_FUNC HPy HPyUnicode_Substring(HPyContext *ctx, HPy obj, HPy_ssize_t start, HPy_ssize_t end) {
     return ctx->ctx_Unicode_Substring ( ctx, obj, start, end ); 
}

HPyAPI_FUNC int HPyList_Check(HPyContext *ctx, HPy h) {
     return ctx->ctx_List_Check ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyList_New(HPyContext *ctx, HPy_ssize_t len) {
     return ctx->ctx_List_New ( ctx, len ); 
}

HPyAPI_FUNC int HPyList_Append(HPyContext *ctx, HPy h_list, HPy h_item) {
     return ctx->ctx_List_Append ( ctx, h_list, h_item ); 
}

HPyAPI_FUNC int HPyDict_Check(HPyContext *ctx, HPy h) {
     return ctx->ctx_Dict_Check ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyDict_New(HPyContext *ctx) {
     return ctx->ctx_Dict_New ( ctx ); 
}

HPyAPI_FUNC HPy HPyDict_Keys(HPyContext *ctx, HPy h) {
     return ctx->ctx_Dict_Keys ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyDict_GetItem(HPyContext *ctx, HPy op, HPy key) {
     return ctx->ctx_Dict_GetItem ( ctx, op, key ); 
}

HPyAPI_FUNC int HPyTuple_Check(HPyContext *ctx, HPy h) {
     return ctx->ctx_Tuple_Check ( ctx, h ); 
}

HPyAPI_FUNC HPy HPyTuple_FromArray(HPyContext *ctx, HPy items[], HPy_ssize_t n) {
     return ctx->ctx_Tuple_FromArray ( ctx, items, n ); 
}

HPyAPI_FUNC int HPySlice_Unpack(HPyContext *ctx, HPy slice, HPy_ssize_t *start, HPy_ssize_t *stop, HPy_ssize_t *step) {
     return ctx->ctx_Slice_Unpack ( ctx, slice, start, stop, step ); 
}

HPyAPI_FUNC HPy HPyContextVar_New(HPyContext *ctx, const char *name, HPy default_value) {
     return ctx->ctx_ContextVar_New ( ctx, name, default_value ); 
}

HPyAPI_FUNC int HPyContextVar_Get(HPyContext *ctx, HPy context_var, HPy default_value, HPy *result) {
     return ctx->ctx_ContextVar_Get ( ctx, context_var, default_value, result ); 
}

HPyAPI_FUNC HPy HPyContextVar_Set(HPyContext *ctx, HPy context_var, HPy value) {
     return ctx->ctx_ContextVar_Set ( ctx, context_var, value ); 
}

HPyAPI_FUNC HPy HPyImport_ImportModule(HPyContext *ctx, const char *name) {
     return ctx->ctx_Import_ImportModule ( ctx, name ); 
}

HPyAPI_FUNC HPy HPyCapsule_New(HPyContext *ctx, void *pointer, const char *name, HPyCapsule_Destructor destructor) {
     return ctx->ctx_Capsule_New ( ctx, pointer, name, destructor ); 
}

HPyAPI_FUNC void *HPyCapsule_Get(HPyContext *ctx, HPy capsule, _HPyCapsule_key key, const char *name) {
     return ctx->ctx_Capsule_Get ( ctx, capsule, key, name ); 
}

HPyAPI_FUNC int HPyCapsule_IsValid(HPyContext *ctx, HPy capsule, const char *name) {
     return ctx->ctx_Capsule_IsValid ( ctx, capsule, name ); 
}

HPyAPI_FUNC int HPyCapsule_Set(HPyContext *ctx, HPy capsule, _HPyCapsule_key key, void *value) {
     return ctx->ctx_Capsule_Set ( ctx, capsule, key, value ); 
}

HPyAPI_FUNC HPy HPy_FromPyObject(HPyContext *ctx, cpy_PyObject *obj) {
     return ctx->ctx_FromPyObject ( ctx, obj ); 
}

HPyAPI_FUNC cpy_PyObject *HPy_AsPyObject(HPyContext *ctx, HPy h) {
     return ctx->ctx_AsPyObject ( ctx, h ); 
}

HPyAPI_FUNC void _HPy_CallRealFunctionFromTrampoline(HPyContext *ctx, HPyFunc_Signature sig, HPyCFunction func, void *args) {
     ctx->ctx_CallRealFunctionFromTrampoline ( ctx, sig, func, args ); 
}

HPyAPI_FUNC HPyListBuilder HPyListBuilder_New(HPyContext *ctx, HPy_ssize_t initial_size) {
     return ctx->ctx_ListBuilder_New ( ctx, initial_size ); 
}

HPyAPI_FUNC void HPyListBuilder_Set(HPyContext *ctx, HPyListBuilder builder, HPy_ssize_t index, HPy h_item) {
     ctx->ctx_ListBuilder_Set ( ctx, builder, index, h_item ); 
}

HPyAPI_FUNC HPy HPyListBuilder_Build(HPyContext *ctx, HPyListBuilder builder) {
     return ctx->ctx_ListBuilder_Build ( ctx, builder ); 
}

HPyAPI_FUNC void HPyListBuilder_Cancel(HPyContext *ctx, HPyListBuilder builder) {
     ctx->ctx_ListBuilder_Cancel ( ctx, builder ); 
}

HPyAPI_FUNC HPyTupleBuilder HPyTupleBuilder_New(HPyContext *ctx, HPy_ssize_t initial_size) {
     return ctx->ctx_TupleBuilder_New ( ctx, initial_size ); 
}

HPyAPI_FUNC void HPyTupleBuilder_Set(HPyContext *ctx, HPyTupleBuilder builder, HPy_ssize_t index, HPy h_item) {
     ctx->ctx_TupleBuilder_Set ( ctx, builder, index, h_item ); 
}

HPyAPI_FUNC HPy HPyTupleBuilder_Build(HPyContext *ctx, HPyTupleBuilder builder) {
     return ctx->ctx_TupleBuilder_Build ( ctx, builder ); 
}

HPyAPI_FUNC void HPyTupleBuilder_Cancel(HPyContext *ctx, HPyTupleBuilder builder) {
     ctx->ctx_TupleBuilder_Cancel ( ctx, builder ); 
}

HPyAPI_FUNC HPyTracker HPyTracker_New(HPyContext *ctx, HPy_ssize_t size) {
     return ctx->ctx_Tracker_New ( ctx, size ); 
}

HPyAPI_FUNC int HPyTracker_Add(HPyContext *ctx, HPyTracker ht, HPy h) {
     return ctx->ctx_Tracker_Add ( ctx, ht, h ); 
}

HPyAPI_FUNC void HPyTracker_ForgetAll(HPyContext *ctx, HPyTracker ht) {
     ctx->ctx_Tracker_ForgetAll ( ctx, ht ); 
}

HPyAPI_FUNC void HPyTracker_Close(HPyContext *ctx, HPyTracker ht) {
     ctx->ctx_Tracker_Close ( ctx, ht ); 
}

HPyAPI_FUNC void HPyField_Store(HPyContext *ctx, HPy target_object, HPyField *target_field, HPy h) {
     ctx->ctx_Field_Store ( ctx, target_object, target_field, h ); 
}

HPyAPI_FUNC HPy HPyField_Load(HPyContext *ctx, HPy source_object, HPyField source_field) {
     return ctx->ctx_Field_Load ( ctx, source_object, source_field ); 
}

HPyAPI_FUNC void HPy_ReenterPythonExecution(HPyContext *ctx, HPyThreadState state) {
     ctx->ctx_ReenterPythonExecution ( ctx, state ); 
}

HPyAPI_FUNC HPyThreadState HPy_LeavePythonExecution(HPyContext *ctx) {
     return ctx->ctx_LeavePythonExecution ( ctx ); 
}

HPyAPI_FUNC void HPyGlobal_Store(HPyContext *ctx, HPyGlobal *global, HPy h) {
     ctx->ctx_Global_Store ( ctx, global, h ); 
}

HPyAPI_FUNC HPy HPyGlobal_Load(HPyContext *ctx, HPyGlobal global) {
     return ctx->ctx_Global_Load ( ctx, global ); 
}

HPyAPI_FUNC void _HPy_Dump(HPyContext *ctx, HPy h) {
     ctx->ctx_Dump ( ctx, h ); 
}

HPyAPI_FUNC int HPyType_CheckSlot(HPyContext *ctx, HPy type, HPyDef *value) {
     return ctx->ctx_Type_CheckSlot ( ctx, type, value ); 
}

