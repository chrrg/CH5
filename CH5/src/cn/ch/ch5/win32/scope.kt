package cn.ch.ch5.win32

import cn.ch.ch5.*
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class scope(var win32: win32, var parent: scope?,var name:String) {

    var variable:ArrayList<Variable>
    var classList:ArrayList<scope>
    var function:ArrayList<func_block>
//    var children:ArrayList<scope>
    var type:ArrayList<basicType>
    var size:Int=0
//    var codeBlock:codeBlock=codeBlock()
    var func_const: fun_block
    var func_static: fun_block
    var func_init: fun_block
    var list_api:ArrayList<Win32Api>
    var offset:Int=0
    init {
        func_const= fun_block(arrayOf(), codeBlock(lightScope(this, null)))//常量写入区
        func_static= fun_block(arrayOf(), codeBlock(lightScope(this, null)))//静态代码块
        func_init= fun_block(arrayOf(), codeBlock(lightScope(this, null)))//非静态代码块

        variable= ArrayList()
        classList=ArrayList()
        function= ArrayList()
//        children= ArrayList()
        type= ArrayList()
        list_api= ArrayList()
    }
    fun get_api(name:String,path:String): import_api {
        val apath=path.toUpperCase()
        for(i2 in win32.import_api){
            if(i2.path==apath && name==i2.name)
                return i2
        }
        val o= import_api(name, apath)
        win32.import_api.add(o)
        return o
    }
    fun hasVarName(varName:String): Boolean {
        for(i in variable)
            if(i.name==varName)return true
        return false
    }
    fun varGet(varName:String): Variable {
        for(i in variable)
        if(i.name==varName)return i
        throw Exception("compiler: variable($varName) is not defined")
    }
    fun addApi(varName:String,api: import_api){
        list_api.add(Win32Api(varName, api))
//        for(i in variable)
//            if(i.name==varName)throw Exception("compiler: symbol is defined by variable!")
//        val v=Variable()
//        v.name=varName
//        v.type=getType("api")
//        v.value=variable_api(api)
//        variable.add(v)
    }
//    fun calcSize(){
//        var totalSize=0
//        for(i in variable)
//            totalSize+=i.type!!.size
//        size=totalSize
//    }
    fun addVar(varName:String,type: basicType): Variable {
        for(i in variable)
            if(i.name==varName)
                throw Exception("compiler: symbol is defined by variable!")
        val v= Variable()
        v.name=varName
        v.type=type
        v.offset=size
        size+=type.size
        variable.add(v)
        return v
    }
    fun getType(typeName:String): basicType {
        for(i in type)
            if(i.name==typeName)
                return i
        val myParent=parent
        if(myParent!=null)
            return myParent.getType(typeName)
        throw Exception("compiler: type is not found!")
    }
    fun getType(ch5type:ch5_type):basicType{
        return getType(ch5type.ident)
//        for(i in type)
//            if(i.name==ch5type.ident)
//                return i
//        throw Exception("compiler: type is not found!")
    }
    fun getTypeByString(str:String): basicType {
        //获取基本类型
        return getType(str)
//        for(i in type){
//            if(i.name==str)return i
//        }
//        throw Exception("compiler: not found type")
    }
    fun err(){
        throw Exception("compiler: unsupport features!")
    }
    fun getExprRetType(expr: ast_object): basicType {
        if(expr is ast_nodeString){
            return getTypeByString("string")
        }else if(expr is ast_nodeInt){
            return getTypeByString("dword")
        }else throw CompilerException("compiler: expr has not a type",expr.token)
    }
//    fun preParse(ast: ArrayList<ast_object>){
//        //浅层提前func
//        for(i in ast){
//            if(i is ast_func){
//                //提前编译
//                val block=codeBlock()//新建一个空的代码块
//                val func=func_block(i.name,i.param,block)
//                parseFunc(func,i.block)
//                function.add(func)
//            }
//        }
//    }

//    fun stringToAddr(node:ast_object){
//        if(node is ast_nodeString){
//            node.code=win32.addConstantString(node.value);
//        }
//    }
    fun use(str:String): Variable {
        if(hasVarName(str)){//说明填的是变量名
            val var_obj=varGet(str).used()
            return var_obj
        }else throw Exception("compiler: variable($str) is not defined")
    }
    fun parseAst(ast:ArrayList<ast_object>){
        for(i in ast){
            if(i is ast_import){
                val name=i.name
                val path=i.path
                val api=get_api(name,path)
                addApi(i.alias,api)

            }else if(i is ast_class){
                val body=i.body
                if(body==null)throw CompilerException("class里面是空的，那你写它干啥？",i.token)
                val subScope= scope(win32, this,i.name)
                subScope.parse(body.arr)
                for(i2 in classList)
                    if(i2.name==i.name)
                        throw CompilerException("你的class重复定义了",i.token)
                classList.add(subScope)
            }else if(i is ast_var){
                for(i2 in i.list){
                    val name=i2.name
                    val type=i2.type
                    val value=i2.value
                    var typeResult: basicType
//                    stringToAddr(value);

                    if(value!=null){

                        val valueType=getExprRetType(value)//获取表达式返回值类型 有可能存在后面才定义函数的情况
//                        if(type!=null){//自定义了类型

//                            if(valueType.name!=type.ident[0]){

//                                throw Exception("compiler: todo features")
//                            }else{
//                                type=valueType;
//                            }

//                        }else{
                            typeResult=valueType
//                            type=valueType;
//                        }
                    }else{
                        //没有值
                        if(type!=null){

                            typeResult=getTypeByString(type.ident)
//                            if(type is ch5_type){
//                                type=getTypeByString(type.ident[count(type.ident)-1]);
//                            }
                            //有类型
                            //无需处理
                        }else{//不允许没有类型又没有值的情况
                            throw CompilerException("compiler: null need a type!",i.token)
                        }
                    }
                    when (value) {
                        null -> {
                            addVar(name,typeResult)
                        }
                        is ast_nodeInt -> {
                            func_const.block.addCode("mov",arrayOf(asm_register_eax(),asm_int(value.value)))
                            func_const.block.addCode("mov",arrayOf(asm_objectIdent(this,addVar(name,typeResult)), asm_register_eax()))
                        }
                        is ast_nodeString -> {
                            func_const.block.addCode("mov",arrayOf(asm_register_eax(), asm_constString(win32.addConstantString(value.value))))
                            func_const.block.addCode("mov",arrayOf(asm_objectIdent(this,addVar(name,typeResult)), asm_register_eax()))
                        }
                        is ast_nodeDouble -> {
                            addVar(name,typeResult)
                            throw CompilerException("err",i.token)
                        }
//                        is ast_nodeInt -> addVar(name,typeResult,variable_int(value))
//                        is ast_nodeString -> addVar(name, typeResult, variable_string(win32.addConstantString(value.value)))
//                        is ast_nodeDouble -> addVar(name,typeResult,variable_double(value))
                        else -> throw CompilerException("err",i.token)
                    }
                }
            }else if(i is ast_init){
                //构造函数
                parseFunc(func_init,i.arr,func_init.block.lightScope)

//                func_init=codeBlock()
                //todo
//                i.arr
            }else if(i is ast_static){
                //静态初始化函数
                parseFunc(func_static,i.arr,func_static.block.lightScope)
//                func_static= codeBlock()

            }else if(i is ast_func){
//                提前编译
                var type:ArrayList<fun_param>
                type= arrayListOf()
                for(i2 in i.param){
                    type.add(fun_param(i2.name,getType(i2.type!!)))
//                    i2.name//参数名称
                }
                val func=func_block(i.name, type.toTypedArray(),codeBlock(lightScope(this, null)))//定义一个新的函数体
                val block=i.block
                if(block!=null) {
                    parseFunc(func, block.arr,func.block.lightScope)
                }
                function.add(func)
            }else if(i is ast_use){
                val sb = StringBuilder()
                val input: InputStream = BufferedInputStream(FileInputStream(i.path))
                var len = 0
                val temp = ByteArray(1024)
                while (input.read(temp).also({ len = it }) != -1)sb.append(String(temp, 0, len))
                val code:String=sb.toString()
                val control=parseControl(code,0,false)
                val fra=codeBody()
                Tokenizer.pre1(fra,control,null)
                val ast=AstParse.codeList(fra.arr)
                parseAst(ast)

            } else{//todo asm
                throw CompilerException("暂不支持",i.token)
//                execExpr(i,codeBlock)
            }
        }
    }

    fun parse(ast:ArrayList<ast_object>){//解析一个作用域
//        preParse(ast)//
        if(win32.allScope.contains(this))throw Exception("不允许重复解析作用域！")
        win32.allScope.add(this)
//        addVar("this",getType("dword"))
        parseAst(ast)//解析class的ast
//        calcSize()
    }
    fun parseFunc(funcBlock: fun_block, astBlock: ArrayList<ast_object>,lightScope:lightScope) {
        for(i in astBlock){
            when (i) {
                else -> {
                    execExpr(i,lightScope)
                }
            }
        }

    }
//    fun registerOrIdent(str:String): asm_param {
//        val register=win32.getRegister(str)
//        if(register!=null)return register
//        return asm_ident(use(str))
//    }
//    fun getIdent(str:String): asm_ident{
//        return asm_ident(use(str))
//    }
    fun hasIdent(str:String,lightScope:lightScope):asm_param?{
        if(lightScope.top==this){//说明是找自己的东西
            val funBlock=lightScope.funBlock
            val funident=lightScope.getIdent(str)//轻作用域里面的变量
            if(funident!=null)
                return asm_funLocalIdent(funident)//函数中的局部变量
            if(funBlock!=null)
            for(i in funBlock.param)
                if(i.name==str)
                    return asm_funParamIdent(i)//函数入参
            if(str=="me"){
                return asm_funIdent(lightScope.funBlock!!)
            }else if(str=="this"){
                return asm_class_static(lightScope.top)
            }
        }
        for(i in variable)
            if(i.name==str)
                return asm_objectIdent(this,i)
        for(i in function)
            if(i.name==str)
                return asm_funIdent(i)
        for(i in list_api)
            if(i.name==str)
                return asm_apiIdent(i.api)
        if(str=="parent"){
            val parent=this.parent
            if(parent!=null)
                return asm_class_static(parent)
            else
                return asm_class_static(this)
        }else if(str=="self"){
            return asm_class_static(this)
        }else if(str=="global"){
            val global=win32.global
            if(global!=null)
                return asm_class_static(global)
            else
                return asm_class_static(this)
        }
        for(i in classList)
            if(i.name==str)
                return asm_class_static(i)
        return null
    }
    fun getIdent(str:String,lightScope:lightScope): asm_param {
        val ident=hasIdent(str,lightScope)
        if(ident!=null)return ident
        throw Exception("not found symbol:"+str)
    }
    fun addrToEbx(param: asm_param,lightScope:lightScope){
        val codeBlock=lightScope.funBlock!!.block
        codeBlock.addCode("mov", arrayOf(asm_register_ebx(),param))
    }
    fun execExprAddr(i: ast_object?,lightScope:lightScope): asm_param {
        if(i==null)throw Exception("null?")
//        val codeBlock=lightScope.funBlock!!.block
        when (i) {
            is ast_binary->{
                val op=i.operator
                when (op){
                    is op_dot->{
                        val baseAddr=execExprAddr(i.left,lightScope)
                        when(baseAddr){
                            is asm_class_static->{
                                return baseAddr.value.execExprAddr(i.right,lightScope)
                            }
                        }
                        throw CompilerException("只能是class.variable",i.token)
                    }
                }
            }
            is ast_nodeWord->{
                return getIdent(i.value,lightScope)
            }
        }
        throw CompilerException("err",i.token)
    }
    fun execExpr(i: ast_object?,lightScope:lightScope){//将ast结果输出到eax
        val codeBlock=lightScope.funBlock!!.block
        if(i==null){
            codeBlock.addCode("mov",arrayOf(asm_register_eax(), asm_int(0)))
            return
        }
        when (i) {

            is ast_var -> {
                for(i2 in i.list){
                    val variable= Variable()
                    variable.name=i2.name
                    var valueType: basicType
                    if(i2.type==null){//未指定类型则自动判断值的类型
                        val value=i2.value
                        if(value==null){
                            throw CompilerException("未指定类型需要给定确定的类型",i.token)
                        }else{
                            valueType=getExprRetType(value)
                        }
                    }else {
                        valueType = getTypeByString(i2.type!!.ident)
                    }
                    variable.type=valueType
                    lightScope.variable.add(variable)//轻作用域
                    execExpr(i2.value,lightScope)//将等于号后面的值计算后写入eax
                    codeBlock.addCode("mov", arrayOf(asm_funLocalIdent(variable), asm_register_eax()))//将eax赋值给变量
                }
            }
            is ast_nodeWord -> {
                codeBlock.addCode("mov",arrayOf(asm_register_eax(),getIdent(i.value,lightScope)))
            }
            is ast_nodeInt -> {
                codeBlock.addCode("mov",arrayOf(asm_register_eax(), asm_int(i.value)))
            }
            is ast_nodeString -> {
                val str=win32.addConstantString(i.value)
                codeBlock.addCode("mov",arrayOf(asm_register_eax(), asm_constString(str)))
            }
            is ast_binary ->{
                val op=i.operator
                when(op){
                    is op_comma ->{//，
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                    }
                    is op_add ->{//+
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("add",arrayOf(asm_register_eax(), asm_register_edx()))
                    }
                    is op_greater->{//>
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("pop",arrayOf(asm_register_eax()))
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(), asm_register_edx()))
                        val label=codeBlock.applyLabel()
                        val label2=codeBlock.applyLabel()

                        codeBlock.addCode("jg",arrayOf(asm_label(label)))//无符号大于则跳转
                        codeBlock.addCode("mov",arrayOf(asm_register_eax(),asm_int(0)))
                        codeBlock.addCode("jmp",arrayOf(asm_label(label2)))//无符号大于则跳转
                        codeBlock.setLabel(label)
                        codeBlock.addCode("mov",arrayOf(asm_register_eax(),asm_int(1)))
                        codeBlock.setLabel(label2)
                    }
                    is op_greaterEqual->{//>
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("pop",arrayOf(asm_register_eax()))
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(), asm_register_edx()))
                        val label=codeBlock.applyLabel()
                        val label2=codeBlock.applyLabel()

                        codeBlock.addCode("jge",arrayOf(asm_label(label)))//无符号大于则跳转
                        codeBlock.addCode("mov",arrayOf(asm_register_eax(),asm_int(0)))
                        codeBlock.addCode("jmp",arrayOf(asm_label(label2)))//无符号大于则跳转
                        codeBlock.setLabel(label)
                        codeBlock.addCode("mov",arrayOf(asm_register_eax(),asm_int(1)))
                        codeBlock.setLabel(label2)
                    }
                    is op_less->{
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("pop",arrayOf(asm_register_eax()))
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(), asm_register_edx()))
                        val label=codeBlock.applyLabel()
                        val label2=codeBlock.applyLabel()

                        codeBlock.addCode("jl",arrayOf(asm_label(label)))//无符号大于则跳转
                        codeBlock.addCode("mov",arrayOf(asm_register_eax(),asm_int(0)))
                        codeBlock.addCode("jmp",arrayOf(asm_label(label2)))//无符号大于则跳转
                        codeBlock.setLabel(label)
                        codeBlock.addCode("mov",arrayOf(asm_register_eax(),asm_int(1)))
                        codeBlock.setLabel(label2)
                    }
                    is op_lessEqual->{
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("pop",arrayOf(asm_register_eax()))
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(), asm_register_edx()))
                        val label=codeBlock.applyLabel()
                        val label2=codeBlock.applyLabel()

                        codeBlock.addCode("jle",arrayOf(asm_label(label)))//无符号大于则跳转
                        codeBlock.addCode("mov",arrayOf(asm_register_eax(),asm_int(0)))
                        codeBlock.addCode("jmp",arrayOf(asm_label(label2)))//无符号大于则跳转
                        codeBlock.setLabel(label)
                        codeBlock.addCode("mov",arrayOf(asm_register_eax(),asm_int(1)))
                        codeBlock.setLabel(label2)
                    }
                    is op_minus ->{//-
                        //3-2
                        //mov eax,3
                        //mov edx,eax
                        //mov eax,2
                        //sub eax,edx

                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
//                        codeBlock.addCode("mov",arrayOf(asm_register_edx(), asm_register_eax()));
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("pop",arrayOf(asm_register_eax()))
                        codeBlock.addCode("sub",arrayOf(asm_register_eax(), asm_register_edx()))
                    }
                    is op_mutil->{
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("pop",arrayOf(asm_register_eax()))
                        codeBlock.addCode("mul",arrayOf( asm_register_edx()))
                    }
                    is op_division->{
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        codeBlock.addCode("pop",arrayOf(asm_register_ebx()))
                        codeBlock.addCode("pop",arrayOf(asm_register_eax()))
                        codeBlock.addCode("mov",arrayOf(asm_register_edx(),asm_int(0)))
                        codeBlock.addCode("div",arrayOf(asm_register_ebx()))
                    }
                    is op_dot ->{
                        if(i.left==null){//call
                            val r=i.right
                            if(r is ast_nodeWord)
                                codeBlock.addCode("invoke",arrayOf(getIdent(r.value,lightScope)))
                            else
                                throw CompilerException("err",i.token)
                            //invoke
                        }else{
                            codeBlock.addCode("mov",arrayOf(asm_register_eax(),execExprAddr(i,lightScope)))
                        }
                    }
                    is op_assign ->{
                        val left=i.left
                        if(left==null){
                            //return
                            execExpr(i.right,lightScope)
                            //todo ret func
//                            codeBlock.addCode("leave",arrayOf());
//                            codeBlock.addCode("ret",arrayOf());
                        }else if(left is ast_nodeWord){
                            execExpr(i.right,lightScope)
                            codeBlock.addCode("mov",arrayOf(getIdent(left.value,lightScope), asm_register_eax()))
                        }else{
                            execExpr(i.right,lightScope)
//                            codeBlock.addCode("push",arrayOf(asm_register_eax()));

                            codeBlock.addCode("mov",arrayOf(execExprAddr(i.left,lightScope),asm_register_eax()))

//                            codeBlock.addCode("mov",arrayOf(asm_register_ebx(), asm_register_eax()));
//                            codeBlock.addCode("mov",arrayOf(, asm_register_eax()));
//                            getIdentListAddr()//将链式地址如 this.a.b.c 解析成地址并存入eax
                        }
                    }
                    is op_equal->{
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
//                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
//                        codeBlock.addCode("pop",arrayOf(asm_register_eax()))
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(), asm_register_edx()))
                        codeBlock.addCode("lahf",arrayOf())
                        codeBlock.addCode("shr",arrayOf(asm_register_eax(),asm_int(0x0E)))//6+8
                        codeBlock.addCode("and",arrayOf(asm_register_eax(),asm_int(1)))
//                        codeBlock.addCode("xor",arrayOf(asm_register_eax(), asm_int(1)));
                    }
                    is op_notEqual->{
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(), asm_register_edx()))
                        codeBlock.addCode("lahf",arrayOf())
                        codeBlock.addCode("shr",arrayOf(asm_register_eax(),asm_int(0x0E)))//6+8
                        codeBlock.addCode("xor",arrayOf(asm_register_eax(),asm_int(1)))
                        codeBlock.addCode("and",arrayOf(asm_register_eax(),asm_int(1)))
                    }
                    is op_and->{
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("and",arrayOf(asm_register_eax(),asm_register_edx()))
                    }
                    is op_or->{
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("or",arrayOf(asm_register_eax(),asm_register_edx()))
                    }
                    is op_andAnd->{
                        execExpr(i.left,lightScope)
                        val label=codeBlock.applyLabel()
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(),asm_int(0)))
                        codeBlock.addCode("jz",arrayOf(asm_label(label)))
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
                        codeBlock.addCode("and",arrayOf(asm_register_eax(),asm_register_edx()))
                        codeBlock.setLabel(label)
                    }
                    is op_orOr->{
                        execExpr(i.left,lightScope)
                        val label=codeBlock.applyLabel()
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(),asm_int(0)))
                        codeBlock.addCode("jnz",arrayOf(asm_label(label)))
//                        codeBlock.addCode("push",arrayOf(asm_register_eax()))//可以优化
                        execExpr(i.right,lightScope)
//                        codeBlock.addCode("pop",arrayOf(asm_register_edx()))
//                        codeBlock.addCode("or",arrayOf(asm_register_eax(),asm_register_edx()))
                        codeBlock.setLabel(label)
                    }
                    is op_left-> {
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("pop",arrayOf(asm_register_ecx()))
                        codeBlock.addCode("shl", arrayOf(asm_register_eax(), asm_register_cl()))
                    }
                    is op_right->{
                        execExpr(i.right,lightScope)
                        codeBlock.addCode("push",arrayOf(asm_register_eax()))
                        execExpr(i.left,lightScope)
                        codeBlock.addCode("pop",arrayOf(asm_register_ecx()))
                        codeBlock.addCode("shr", arrayOf(asm_register_eax(), asm_register_cl()))
                    }
                    else->{
                        throw CompilerException("暂不支持！"+op.symbol,i.token)
                    }
                }
            }
            is ast_call ->{
                execExpr(i.value,lightScope)
                codeBlock.addCode("push",arrayOf(asm_register_eax()))
                val name=i.name
                if(name is ast_nodeWord) {
                    codeBlock.addCode("invoke",arrayOf(getIdent(name.value,lightScope)))
                }else throw CompilerException("暂不支持！",i.token)
//                if(name is ast_nodeWord) {
//
////                if(name is ast_nodeWord) {
////                    codeBlock.addCode("mov", arrayOf(getIdent(name.value,codeBlock), asm_register_eax()));
//                }else throw CompilerException("暂不支持！")
            }
            is ast_block->{
                if(i.arr.size==0)throw CompilerException("空括号？",i.token)
                for(i2 in i.arr){
                    execExpr(i2,lightScope)
                }
            }
            is ast_if->{
                execExpr(i.expr,lightScope)
                val trueBlock=i.trueBlock
                val falseBlock=i.falseBlock
                if(trueBlock!=null){
                    val newlightScope=lightScope(this,lightScope)
                    newlightScope.funBlock=lightScope.funBlock
//                    val size=codeBlock
                    if(falseBlock!=null){
                        val label=codeBlock.applyLabel()
                        val label2=codeBlock.applyLabel()
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(),asm_int(0)))
                        codeBlock.addCode("jz",arrayOf(asm_label(label)))
                        parseFunc(lightScope.funBlock!!,trueBlock.arr,newlightScope)
                        codeBlock.addCode("jmp",arrayOf(asm_label(label2)))
                        codeBlock.setLabel(label)
                        parseFunc(lightScope.funBlock!!,falseBlock.arr,newlightScope)
                        codeBlock.setLabel(label2)
                    }else{
                        val label=codeBlock.applyLabel()
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(),asm_int(0)))
                        codeBlock.addCode("jz",arrayOf(asm_label(label)))
                        parseFunc(lightScope.funBlock!!,trueBlock.arr,newlightScope)
                        codeBlock.setLabel(label)
                    }
                }else{
                    val newlightScope=lightScope(this,lightScope)
                    newlightScope.funBlock=lightScope.funBlock
                    if(falseBlock!=null){
                        val label=codeBlock.applyLabel()
                        codeBlock.addCode("cmp",arrayOf(asm_register_eax(),asm_int(0)))
                        codeBlock.addCode("jnz",arrayOf(asm_label(label)))
                        parseFunc(lightScope.funBlock!!,falseBlock.arr,newlightScope)
                        codeBlock.setLabel(label)
                    }
                }



//                i.falseBlock
            }
            is ast_for->{
                val expr=i.expr
                if(expr!=null){//for(var i=0;i<10;i++)
                    val newlightScope=lightScope(this,lightScope)
                    newlightScope.funBlock=lightScope.funBlock
                    if(expr.arr.size>0)execExpr(expr.arr[0],newlightScope)
                    val label = codeBlock.applyLabel()
                    val label2 = codeBlock.applyLabel()
                    codeBlock.setLabel(label2)
                    if(expr.arr.size>1) {
                        execExpr(expr.arr[1], newlightScope)
                        codeBlock.addCode("cmp", arrayOf(asm_register_eax(), asm_int(0)))
                        codeBlock.addCode("jz", arrayOf(asm_label(label)))
                    }
                    execExpr(i.body,newlightScope)
                    if(expr.arr.size>2)execExpr(expr.arr[2],newlightScope)
                    codeBlock.addCode("jmp", arrayOf(asm_label(label2)))
                    codeBlock.setLabel(label)
                }else throw CompilerException("err",i.token)

//                i.body
            }
            is ast_while->{
                /*
                while 1==1{

                }
                * */
                val label = codeBlock.applyLabel()
                val label2 = codeBlock.applyLabel()
                codeBlock.setLabel(label2)
                execExpr(i.expr,lightScope)//计算while里面的表达式
                codeBlock.addCode("cmp",arrayOf(asm_register_eax(),asm_int(0)))
                codeBlock.addCode("jz",arrayOf(asm_label(label)))//如果为0则false跳转出去
                val newlightScope=lightScope(this,lightScope)
                newlightScope.funBlock=lightScope.funBlock
                execExpr(i.body,newlightScope)
                codeBlock.addCode("jmp", arrayOf(asm_label(label2)))
                codeBlock.setLabel(label)
            }
            is ast_unary->{
                val op=i.operator
                when(op){
                    is op_inc->{
                        if(i.left!=null&&i.right==null){
                            //a++
                            execExpr(i.left,lightScope)//计算a到eax
                            codeBlock.addCode("mov",arrayOf(asm_register_edx(),asm_register_eax()))//eax复制到edx
                            codeBlock.addCode("add",arrayOf(asm_register_edx(),asm_int(1)))//edx=edx+1
                            codeBlock.addCode("mov",arrayOf(execExprAddr(i.left,lightScope),asm_register_edx()))//a=edx
                        }else if(i.left==null&&i.right!=null){
                            //++a
                            execExpr(i.right,lightScope)
                            codeBlock.addCode("add",arrayOf(asm_register_eax(),asm_int(1)))
                            codeBlock.addCode("mov",arrayOf(execExprAddr(i.right,lightScope),asm_register_eax()))
                        }else{
                            throw Exception("a++b这样也行???")
                        }
                    }
                    is op_dec->{
                        if(i.left!=null&&i.right==null){
                            //a--
                            execExpr(i.left,lightScope)//计算a到eax
                            codeBlock.addCode("mov",arrayOf(asm_register_edx(),asm_register_eax()))//eax复制到edx
                            codeBlock.addCode("sub",arrayOf(asm_register_edx(),asm_int(1)))//edx=edx+1
                            codeBlock.addCode("mov",arrayOf(execExprAddr(i.left,lightScope),asm_register_edx()))//a=edx
                        }else if(i.left==null&&i.right!=null){
                            //--a
                            execExpr(i.right,lightScope)
                            codeBlock.addCode("sub",arrayOf(asm_register_eax(),asm_int(1)))
                            codeBlock.addCode("mov",arrayOf(execExprAddr(i.right,lightScope),asm_register_eax()))
                        }else{
                            throw Exception("a--b这样也行???")
                        }
                    }
                }
            }
            else->{
                throw CompilerException("暂不支持！",i.token)
            }

        }
    }
//        if(!i){
//            codeBlock.addCode("mov12",["eax",0]);
//            return;
//        }else if(is_object(i)){//说明是对象
//            if(i.type==0){
//                codeBlock.addCode("mov13",["eax",use(i.code)]);
//            }else if(i.type==9){//常数9
//                codeBlock.addCode("mov12",["eax",i.code]);
//            }else throw CompilerException("err");
//        }else if(i["type"]==","){
//            execExpr(i["left"]);//计算表达式
//            codeBlock.addCode("push1",["eax"]);
//            execExpr(i["right"]);
//        }else if(i["type"]=="-"){
//            if(i["left"]==null){//说明左边空的
//                if(i["right"]==null){//说明右边空的
//                    throw CompilerException("compiler null+null?");
//                }else if(is_object(i["right"])){//说明是对象
//                    if(i["right"].type==0){
//                        codeBlock.addCode("mov13",["eax",use(i["right"].code)]);
//                        codeBlock.addCode("neg1",["eax"]);
//                    }else if(i["right"].type==9){//常数9
//                        codeBlock.addCode("mov12",["eax",-i["right"].code]);
//                    }else throw CompilerException("err");
//                }else{//说明右边是表达式
//                    execExpr(i["right"]);
//                }
//            }else if(is_object(i["left"])){//说明是对象
//                if(i["left"].type==9){//常数9
//                    if(i["right"]==null){//说明右边空的
//                        throw CompilerException("compiler expr-null?");
//                    }else if(is_object(i["right"])){//说明是对象
//                        if(i["right"].type==9){//常数9
//                            codeBlock.addCode("mov12",["eax",i["left"].code-i["right"].code]);
//                        }else throw CompilerException("err");
//                    }else{//说明右边是表达式
//                        execExpr(i["right"]);
//                        codeBlock.addCode("add12",["eax",-i["left"].code]);
//                    }
//                }
//            }else{//说明左边是一个表达式
//                if(i["right"]==null){//说明右边空的
//                    throw CompilerException("compiler: expr-null?", 1);
//                }else if(is_object(i["right"])){//说明是对象
//                    if(i["right"].type==9){//常数9
//                        execExpr(i["left"]);
//                        codeBlock.addCode("add12",["eax",-i["right"].code]);
//                    }else throw CompilerException("err");
//                }else{
//                    execExpr(i["left"]);//mov eax,-12
//                    codeBlock.addCode("push1",["eax"]);
//                    execExpr(i["right"]);
//                    codeBlock.addCode("pop1",["ebx"]);
//                    codeBlock.addCode("sub11",["ebx","eax"]);
//                    codeBlock.addCode("mov11",["eax","ebx"]);
//                }
//            }
//        }else if(i["type"]=="+"){
//            // var_dump(i);
//            if(i["left"]==null){//说明左边空的
//                if(i["right"]==null){//说明右边空的
//                    throw CompilerException("compiler null+null?");
//                }else if(is_object(i["right"])){//说明是对象
//                    if(i["right"].type==9){//常数9
//                        codeBlock.addCode("mov12",["eax",i["right"].code]);
//                    }else throw CompilerException("err");
//                }else{//说明右边是表达式
//                    execExpr(i["right"]);
//                }
//            }else if(is_object(i["left"])){//说明是对象
//                if(i["left"].type==9){//常数9
//                    if(i["right"]==null){//说明右边空的
//                        throw CompilerException("compiler expr+null?");
//                    }else if(is_object(i["right"])){//说明是对象
//                        if(i["right"].type==9){//常数9
//                            codeBlock.addCode("mov12",["eax",i["left"].code+i["right"].code]);
//                        }else throw CompilerException("err");
//                    }else{//说明右边是表达式
//                        execExpr(i["right"]);
//                        codeBlock.addCode("add12",["eax",i["left"].code]);
//                    }
//                }
//            }else{//说明左边是一个表达式
//                if(i["right"]==null){//说明右边空的
//                    throw CompilerException("err", 1);
//                }else if(is_object(i["right"])){//说明是对象
//                    if(i["right"].type==9){//常数9
//                        execExpr(i["left"]);
//                        codeBlock.addCode("add12",["eax",i["right"].code]);
//                    }else throw CompilerException("err");
//                }else{
//                    execExpr(i["left"]);//mov eax,-12
//                    codeBlock.addCode("push1",["eax"]);
//                    execExpr(i["right"]);
//                    codeBlock.addCode("pop1",["ebx"]);
//                    codeBlock.addCode("add11",["eax","ebx"]);
//                }
//            }
//        }else if(i["type"]=="call"){
//            /*if(i["value"].type==","){
//                execExpr(i["value"].left);//计算表达式
//                codeBlock.addCode("push1",["eax"]);
//
//            }*/
//            execExpr(i["value"]);//计算表达式
//
//            codeBlock.addCode("push1",["eax"]);
//            codeBlock.addCode("invoke3",[use(i["name"].code)]);
//            // var_dump(i);die;
//        }else if(i["type"]=="="){//a=123+456
//            // var_dump(i);
//            if(i["left"].type==0){
//                if(is_object(i["right"])){
//                    // var_dump(i);
//                    if(i["right"].type==0){
//                        codeBlock.addCode("mov13",["eax",use(i["right"].code)]);
//                        codeBlock.addCode("mov31",[use(i["left"].code),"eax"]);
//                    }else throw CompilerException("compiler: err");
//                }else{//右边是表达式
//                    execExpr(i["right"]);
//                    codeBlock.addCode("mov31",[use(i["left"].code),"eax"]);
//                }
//            }else throw CompilerException("compiler: err");
//        }else if(i["type"]=="."){//不带参数的调用函数
//            codeBlock.addCode("invoke3",[use(i["right"].code)]);
//        }else throw CompilerException("i[type] err");
//    }
    fun registerType(name:String,size:Int){//注册基本类型
        type.add(basicType(name, size))
    }
}