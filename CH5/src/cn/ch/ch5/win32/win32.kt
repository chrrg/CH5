package cn.ch.ch5.win32

import cn.ch.ch5.ast_fun_param
import cn.ch.ch5.ast_object
import sun.nio.cs.ext.GBK

const val CH_CODE=0x20
const val CH_INITIALIZED_DATA= 0x40
const val CH_UNINITIALIZED_DATA= 0x80
const val CH_MEM_DISCARDABLE= 0x2000000
const val CH_MEM_NOT_CHACHED= 0x4000000
const val CH_MEM_NOT_PAGED= 0x8000000
const val CH_MEM_SHARED= 0x10000000
const val CH_MEM_EXECUTE= 0x20000000
const val CH_MEM_READ= 0x40000000
const val CH_MEM_WRITE= 0x80000000

class waitFix_class(var name:String,var offset:Int)
class wantFix_class(var name:String,var size:Int,var value:Int,var type:Int,var param:build_section?,var offset:Int=0)

internal interface ch5_platform {
    val name: String?
    val version: String?
    val bit: Int
}

//open class variable_object(var used:Boolean)
//class variable_string(var value:constantString,used: Boolean=false):variable_object(used)
//class variable_api(var value:import_api,used: Boolean=false):variable_object(used)
//class variable_int(var value:ast_nodeInt,used: Boolean=false):variable_object(used)
//class variable_double(var value:ast_nodeDouble,used: Boolean=false):variable_object(used)
//class variable_null(used: Boolean=false):variable_object(used)

class Variable{//全局变量暂存区（不是函数里面的变量都是variable，函数里面的variable是堆栈段）
    var name:String=""
    var type:basicType?=null
//    var value:variable_object?=null
    var used:Boolean=false//false说明没用过 则不写入
    var offset:Int=0//全局变量为data偏移地址//函数变量为栈偏移地址
    fun used(): Variable {
        used=true
//        if(type?.name=="api")
//            value?.used=true
        return this
    }
}
open class asm_param()

open class asm_register(var value:String):asm_param()
class asm_register_eax():asm_register("eax")
class asm_register_ebx():asm_register("ebx")
class asm_register_ecx():asm_register("ecx")
class asm_register_edx():asm_register("edx")
class asm_register_cl():asm_register("cl")
class asm_label(var label:Int,var offset:Int=0):asm_param()
class asm_int(var value:Int):asm_param()
open class asm_ident(var value:Variable):asm_param()
class asm_funIdent(var value:fun_block):asm_param()//函数
class asm_funParamIdent(var value:fun_param):asm_param()//函数的参数
class asm_funLocalIdent(value:Variable):asm_ident(value)//函数的局部变量
class asm_apiIdent(var value:import_api):asm_param()//类导入的api alias名
//class asm_staticIdent(var scope:scope,value:Variable):asm_ident(value)//静态class的偏移地址
class asm_objectIdent(var scope:scope,value:Variable):asm_ident(value)//实例化的class的偏移地址
class asm_class_static(var value:scope):asm_param()//静态类
class asm_class_new(var scope:scope,offset:Int):asm_param()//实例化的类

class asm_constString(var value:constantString):asm_param()
class codeAsm(var command:String,var param:Array<asm_param>)
class codeBlock(var lightScope:lightScope){//顺序执行的汇编代码流
    var code:ArrayList<codeAsm>
    var label:ArrayList<Int>
    var waitFix:ArrayList<Array<Int>>
    var labelOffset: HashMap<Int,Int>
    init{
        code=ArrayList()
        label= ArrayList()
        waitFix= ArrayList()
        labelOffset= hashMapOf()
    }
    fun addCode(command:String,param:Array<asm_param>){
        code.add(codeAsm(command,param))
    }
    fun applyLabel(): Int {
        label.add(0)
        return label.size-1
    }
    fun setLabel(id: Int){
        label[id]=code.size
    }

}
class fun_param(var name:String,var type:basicType,var offset: Int =0)
open class fun_block(var param:Array<fun_param>, var block:codeBlock){
    var offset:Int=0
    init{
        block.lightScope.funBlock=this
    }

//    fun getSize(): Int {
//        var size=0
//        for(i in param)size += i.type.size
//        return size
//    }
    fun hasCode(): Boolean {
        return block.code.size>0
    }
}

class func_block(var name:String, param: Array<fun_param>, block: codeBlock):fun_block(param, block)

class basicType(var name:String,var size:Int)
class Win32Api(var name:String,var api:import_api)
class import_api(var name: String,var path:String, var used: Boolean = false,var offset:Int=0){
    fun use(){
        used=true
    }
}
class constantString(var string:String,var bytes:ByteArray,var offset:Int,var used:Boolean=false)
class win32 : ch5_platform {
    var import_api:ArrayList<import_api>
    var constantStringPool:ArrayList<constantString>//常量池
    var constantStringSize:Int=0
    var global:scope?=null
    var build:build_section?=null
    var sections:build_section?=null
    var allScope:ArrayList<scope>
    init{
        import_api= ArrayList()
        constantStringPool= ArrayList()
        allScope= ArrayList()
    }
    override val name: String?
        get() = "win32"

    override val version: String?
        get() = "1.0"

    override val bit: Int
        get() = 64

    fun addConstantString(str:String): constantString {
        for(i in constantStringPool){
            if(i.string==str)return i
        }
        val encodeString=str.toByteArray(charset("GBK"))
        val n=constantString(str,encodeString,constantStringSize)
        constantStringPool.add(n)
        constantStringSize+=encodeString.size+1
        return n
    }
//    fun sectionFix(value,param){
//        return 0x666
//    }
    fun movIdentToEbx(obj: asm_param,sections: build_section){
        val data=sections.get(0)
        val code=sections.get(1)
        if(obj is asm_objectIdent){
            code.byte(0xBB)//mov ebx,0
            code.wantFix("",4,obj.value.offset,0,data,code.size())
            code.dword(0)
            code.byte(0x03)//mov ebx,[0]
            code.byte(0x1D)
            code.wantFix("",4,obj.scope.offset,3,data,code.size())
            code.dword(0)
        }else if(obj is asm_funParamIdent){
            code.byte(0x8D)
            code.byte(0x9D)//ebx
            code.dword(obj.value.offset+8)
        }else if(obj is asm_funLocalIdent){//局部变量
            code.byte(0x8D)
            code.byte(0x9D)//ebx
            code.dword(-obj.value.offset-4)
//            throw Exception("err")
        }else if(obj is asm_ident){

            throw Exception("err")
        }else throw Exception("err")
    }
    fun writeFunBlock(block:fun_block,sections:build_section){//写入函数
        val codeblock=block.block
        val data=sections.get(0)
        val code=sections.get(1)
        val importTable=sections.get(2)
        val offset=code.size()//代码偏移量
        block.offset=offset
        var size=0
        for(i in block.param){
            i.offset=size
            size+=i.type.size
        }
        block.block.lightScope.setVariableOffset(0)

//        val size=block.getSize()//参数的大小
        val size2=block.block.lightScope.getChildSize()//局部变量大小
        code.byte(0xC8)//enter
        code.word(size2)//局部变量大小//sub esp,size2
        code.byte(0)//固定
//        getSize
        //下面是label
        //下面开始循环
        var index=0
        for(i in codeblock.code){
            for(j in 0 until codeblock.label.size){
                if(codeblock.label[j]>=index){
                    codeblock.labelOffset[j]=code.size()
                }
            }
            index++
            when(i.command){
                "mov"->{
                    val p0=i.param[0]
                    val p1=i.param[1]
                    if(p0 is asm_register && p1 is asm_register){
                        code.byte(0x8B)
                        if(p0 is asm_register_edx){
                            if(p1 is asm_register_eax){
                                code.byte(0xD0)
                            }else throw Exception("err")
                        }else if(p0 is asm_register_eax){
                            if(p1 is asm_register_edx){
                                code.byte(0xC3)
                            }else throw Exception("err")
                        }else throw Exception("err")
                    }else if(p0 is asm_register && p1 is asm_int){
                        if(p0 is asm_register_eax){
                            code.byte(0xB8)
                            code.dword(p1.value)
                        }else if(p0 is asm_register_edx){
                            code.byte(0xBA)
                            code.dword(p1.value)
                        }else throw Exception("err")
                    }else if(p0 is asm_register && p1 is asm_ident){
                        if(p0 is asm_register_eax){
                            //mov eax,[ebx]
                            movIdentToEbx(p1,sections)
                            code.byte(0x8B)
                            code.byte(0x03)
//                            code.byte(0xA1)
//                            code.wantFix("",4,p1.value.offset,3,data,code.size())
//                            code.dword(0)
                        }else throw Exception("err")
                    }else if(p0 is asm_ident && p1 is asm_register){
                        //mov sss,eax
                        if(p1 is asm_register_eax){
                            //mov [ebx],eax
                            movIdentToEbx(p0,sections)
                            code.byte(0x89)
                            code.byte(0x03)
//                            code.byte(0x89)
//                            code.byte(0x055)
//                            code.wantFix("",4,p0.value.offset,3,data,code.size())
//                            code.dword(0)
                        }else if(p1 is asm_register_edx){
                            //mov [ebx],edx
                            movIdentToEbx(p0,sections)
                            code.byte(0x89)
                            code.byte(0x13)
                        }else throw Exception("err")
                    }else if(p0 is asm_register && p1 is asm_constString){
                        if(p0 is asm_register_eax){
                            code.byte(0xB8)
                            code.wantFix("",4,p1.value.offset,3,data,code.size())
                            code.dword(0)
                        }else throw Exception("err")
                    }else if(p0 is asm_register && p1 is asm_funParamIdent){//入参
                        if(p0 is asm_register_eax){
                            code.byte(0x8B)
                            code.byte(0x85)
                            code.dword(p1.value.offset+8)
                        }else throw Exception("err")
                    }else if(p0 is asm_register && p1 is asm_funLocalIdent){//局部变量
                        if(p0 is asm_register_eax){
                            code.byte(0x8B)
                            code.byte(0x85)
                            code.dword(-p1.value.offset-4)
                        }else throw Exception("err")
                    }else throw Exception("err")
                }
                "invoke"->{
                    val p0=i.param[0]
                    if(p0 is asm_apiIdent){
                        code.word(0x15FF)
                        code.wantFix("",4,p0.value.offset,3,importTable,code.size())
                        code.dword(0)
                    }else if(p0 is asm_funIdent){
                        if(p0.value.offset==0)throw Exception("函数顺序提前？")
                        code.byte(0xE8)
                        code.wantFix("",4,p0.value.offset-code.size()-4,0,code,code.size())
                        code.dword(0)
                    }else if(p0 is asm_ident){
                        throw Exception("err")
//                        movIdentToEbx(p0)
//                        code.word(0x15FF)
//                        code.wantFix("",4,p0.value.offset,3,data,code.size())
//                        code.dword(0)
                    }else throw Exception("err")

                }
                "push"->{
                    val p0=i.param[0]
                    if(p0 is asm_register_eax){
                        code.byte(0x50)
                    }else if(p0 is asm_int){
                        code.byte(0x68)
                        code.dword(p0.value)
                    }else if(p0 is asm_ident){
                        code.word(0x35FF)
                        code.wantFix("",4,p0.value.offset,3,data,code.size())
                        code.dword(0)
                    }else throw Exception("err")
                }
                "add"->{
                    val p0=i.param[0]
                    val p1=i.param[1]
                    if(p0 is asm_register_eax){
                        if(p1 is asm_register_ebx){
                            code.byte(0x03)
                            code.byte(0xC3)
                        }else if(p1 is asm_register_edx){
                            code.byte(0x03)
                            code.byte(0xC2)
                        }else if(p1 is asm_int){
                            code.byte(0x81)
                            code.byte(0xC0)
                            code.dword(p1.value)
                        }else throw Exception("err")
                    }else if(p0 is asm_register_edx){
                        if(p1 is asm_register_eax){
                            code.byte(0x03)
                            code.byte(0xD0)
                        }else if(p1 is asm_int){
                            code.byte(0x81)
                            code.byte(0xC2)
                            code.dword(p1.value)
                        }else throw Exception("err")
                    }else throw Exception("err")
                }
                "sub"->{
                    val p0=i.param[0]
                    val p1=i.param[1]
                    if(p0 is asm_register) {
                        if (p0 is asm_register_ebx) {
                            if (p1 is asm_register_eax) {
                                code.byte(0x2B)
                                code.byte(0xD8)
                            } else throw Exception("err")
                        } else if (p0 is asm_register_eax) {
                            if (p1 is asm_register_edx) {
                                code.byte(0x2B)
                                code.byte(0xC2)
                            } else if (p1 is asm_int) {
                                code.byte(0x81)
                                code.byte(0xE8)
                                code.dword(p1.value)
                            } else throw Exception("err")
                        } else if (p0 is asm_register_edx) {
                            if (p1 is asm_register_eax) {
                                code.byte(0x2B)
                                code.byte(0xD0)
                            } else if (p1 is asm_int) {
                                code.byte(0x81)
                                code.byte(0xEA)
                                code.dword(p1.value)
                            } else throw Exception("err")
                        } else throw Exception("err")
                    } else throw Exception("err")
                }
                "pop"->{
                    val p0=i.param[0]
                    if(p0 is asm_register_eax) {
                        code.byte(0x58)
                    }else if(p0 is asm_register_ebx) {
                        code.byte(0x5B)
                    }else if(p0 is asm_register_ecx) {
                        code.byte(0x59)
                    }else if(p0 is asm_register_edx) {
                        code.byte(0x5A)
                    } else throw Exception("err")
                }
                "jz"->{
                    val p0=i.param[0]
                    if(p0 is asm_label){
                        code.byte(0x0F)
                        code.byte(0x84)
                        codeblock.waitFix.add(arrayOf(p0.label,code.size()))
                        code.dword(0)
                    } else throw Exception("err")
                }
                "jnz"->{
                    val p0=i.param[0]
                    if(p0 is asm_label){
                        code.byte(0x0F)
                        code.byte(0x85)
                        codeblock.waitFix.add(arrayOf(p0.label,code.size()))
                        code.dword(0)
                    } else throw Exception("err")
                }

                "jmp"->{
                    val p0=i.param[0]
                    if(p0 is asm_label){
                        code.byte(0xE9)
                        codeblock.waitFix.add(arrayOf(p0.label,code.size()))
                        code.dword(0)
                    } else throw Exception("err")
                }
                "jg"->{
                    val p0=i.param[0]
                    if(p0 is asm_label){
                        code.byte(0x0F)
                        code.byte(0x8F)
                        codeblock.waitFix.add(arrayOf(p0.label,code.size()))
                        code.dword(0)
                    } else throw Exception("err")
                }
                "jge"->{
                    val p0=i.param[0]
                    if(p0 is asm_label){
                        code.byte(0x0F)
                        code.byte(0x8D)
                        codeblock.waitFix.add(arrayOf(p0.label,code.size()))
                        code.dword(0)
                    } else throw Exception("err")
                }
                "jl"->{
                    val p0=i.param[0]
                    if(p0 is asm_label){
                        code.byte(0x0F)
                        code.byte(0x8C)
                        codeblock.waitFix.add(arrayOf(p0.label,code.size()))
                        code.dword(0)
                    } else throw Exception("err")
                }
                "jle"->{
                    val p0=i.param[0]
                    if(p0 is asm_label){
                        code.byte(0x0F)
                        code.byte(0x8E)
                        codeblock.waitFix.add(arrayOf(p0.label,code.size()))
                        code.dword(0)
                    } else throw Exception("err")
                }
                "mul"->{
                    val p0=i.param[0]
                    if (p0 is asm_register_edx) {
                        code.byte(0xF7)
                        code.byte(0xE2)
                    } else throw Exception("err")
                }
                "div"->{
                    val p0=i.param[0]
                    if (p0 is asm_register_ebx) {
                        code.byte(0xF7)
                        code.byte(0xF3)
                    } else throw Exception("err")
                }
                "lahf"->{
                    code.byte(0x9F)
                }
                "shl"->{
                    val p0=i.param[0]
                    val p1=i.param[1]
                    if (p0 is asm_register_eax) {
                        if (p1 is asm_int) {
                            code.byte(0xC1)
                            code.byte(0xE0)
                            code.byte(p1.value)
                        } else if (p1 is asm_register_cl) {
                            code.byte(0xD3)
                            code.byte(0xE0)
                        } else throw Exception("err")
                    } else throw Exception("err")
                }
                "shr"->{
                    val p0=i.param[0]
                    val p1=i.param[1]
                    if (p0 is asm_register_eax) {
                        if (p1 is asm_int) {
                            code.byte(0xC1)
                            code.byte(0xE8)
                            code.byte(p1.value)
                        } else if (p1 is asm_register_cl) {
                            code.byte(0xD3)
                            code.byte(0xE8)
                        } else throw Exception("err")
                    } else throw Exception("err")
                }
                "and"->{
                    val p0=i.param[0]
                    val p1=i.param[1]
                    if (p0 is asm_register_eax) {
                        if (p1 is asm_int) {
                            code.byte(0x81)
                            code.byte(0xE0)
                            code.dword(p1.value)
                        } else if (p1 is asm_register_edx) {
                            code.byte(0x21)
                            code.byte(0xD0)
                        }else throw Exception("err")
                    } else throw Exception("err")
                }
                "cmp"->{
                    val p0=i.param[0]
                    val p1=i.param[1]
                    if (p0 is asm_register_eax) {
                        if (p1 is asm_register_edx) {
                            code.byte(0x3B)
                            code.byte(0xC2)
                        }else if (p1 is asm_int) {
                            code.byte(0x81)
                            code.byte(0xF8)
                            code.dword(p1.value)
                        } else throw Exception("err")
                    } else if (p0 is asm_register_edx) {
                        if (p1 is asm_register_eax) {
                            code.byte(0x3B)
                            code.byte(0xD0)
                        } else throw Exception("err")
                    }else throw Exception("err")
                }
                "or"->{
                    val p0=i.param[0]
                    val p1=i.param[1]
                    if (p0 is asm_register_eax) {
                        if (p1 is asm_register_edx) {
                            code.byte(0x09)
                            code.byte(0xD0)
                        }else throw Exception("err")
                    }else throw Exception("err")
                }
                "xor"->{
                    val p0=i.param[0]
                    val p1=i.param[1]
                    if (p0 is asm_register_eax) {
                        if (p1 is asm_int) {
                            code.byte(0x81)
                            code.byte(0xF0)
                            code.dword(p1.value)
                        }else throw Exception("err")
                    }else throw Exception("err")
                }
                else->{
                    throw Exception("不支持"+i.command)
                }

            }
        }
        for(i in codeblock.waitFix){
            val num:Int?=codeblock.labelOffset[i[0]]
            if(num==null)throw Exception("err")
            code.wantFix("",4,num-i[1]-4,0,code,i[1])
        }
        code.byte(0xC9)
        if(size==0) {
            code.byte(0xC3)//ret
        }else{
            code.byte(0xc2)//retn
            code.word(size)
        }


    }
    fun parse(ast:ArrayList<ast_object>){//解析整个ast
        //ast 所有源程序的源码
        val scope=scope(this,null,"global")//创建一个全局作用域
        global=scope
        scope.registerType("byte",1)
        scope.registerType("word",2)
        scope.registerType("dword",4)
        scope.registerType("api",4)
        scope.registerType("string",4)
        scope.registerType("fun",4)//注册基本类型
        scope.parse(ast)//解析全局作用域
        // scope;
        //解析ast
        // sectionTable=build_section;//表

        val sections=build_section()
        this.sections=sections
        win32_tool.generateSections(sections)
        val data=sections.get(0)//data
        val code=sections.get(1)//code
        val importTable=sections.get(2)//idata
        //----------------------------------常量字符串----------------------------
        //写入常量字符串 并设置偏移值
        for(i in constantStringPool){
            for(i2 in i.bytes)data.byte(i2.toInt())
            data.byte(0)//截止符号
        }
        val GetProcessHeap=scope.get_api("GetProcessHeap","KERNEL32.DLL")
        val HeapAlloc=scope.get_api("HeapAlloc","KERNEL32.DLL")
        GetProcessHeap.use()
        HeapAlloc.use()
        //------------------------------导入表------------------------------------
        val librariesList=ArrayList<String>()
    
        for(i in import_api){
//            if(!i.used)continue//没使用就跳过
            if(librariesList.contains(i.path))continue
            librariesList.add(i.path)
            importTable.dword(0)//
            importTable.dword(0)//
            importTable.dword(0)//
            importTable.waitFix(i.path+"_NAME")//KERNERL32.dll_NAME
            importTable.dword(0)//
            importTable.waitFix(i.path+"_TABLE")//KERNERL32.dll_TABLE
            importTable.dword(0)//
        }
        if(librariesList.size>0){
            //空白开始
            importTable.dword(0)//
            importTable.dword(0)//
            importTable.dword(0)//
            importTable.dword(0)//
            importTable.dword(0)//
            //空白结束
        }
        for(i in librariesList){//KERNER32.DLL USER32.DLL
            importTable.wantFix(i+"_TABLE",4,importTable.size(),1,importTable)//KERNERL32.dll_TABLE
            for(i2 in import_api){//ALlocConsole MessageBoxA
                if(i==i2.path){
                    i2.offset=importTable.size()
                    importTable.waitFix(i2.name+"_ENTRY")//MessageBoxA_ENTRY
                    importTable.dword(0)
                }
            }
            importTable.dword(0)
        }
        for(i in librariesList){
            importTable.wantFix(i+"_NAME",4,importTable.size(),1,importTable)//KERNERL32.dll_NAME
            val l=i.length
            for(ii in 0 until l)
                importTable.put(1,i[ii].toInt())
            importTable.put(1,0)
        }
        for(i in import_api){
            importTable.wantFix(i.name+"_ENTRY",4,importTable.size(),1,importTable)//MessageBoxA_ENTRY
            importTable.put(2,0)
            val l=i.name.length
            for(ii in 0 until l)
            importTable.put(1,i.name[ii].toInt())
            importTable.put(1,0)
        }
        //写入data和code
//        val staticOffset=data.size()
        code.word(0x15FF)
        code.wantFix("",4,GetProcessHeap.offset,3,importTable,code.size())
        code.dword(0)
        code.byte(0x8B)
        code.byte(0xD8)//mov ebx,eax
        for(i in allScope){
            code.byte(0x68)//push size
            code.dword(i.size)
            code.byte(0x68)//push
            code.dword(8)//HEAP_ZERO_MEMORY
            code.byte(0x53)//push ebx
            code.word(0x15FF)//invoke
            code.wantFix("",4,HeapAlloc.offset,3,importTable,code.size())
            code.dword(0)
            code.byte(0x89)//mov
            code.byte(0x05)//[],eax
            i.offset=data.size()
            code.wantFix("",4,i.offset,3,data,code.size())
            code.dword(0)
            data.dword(0)//写入对象的静态对象的指针
        }
        //这里写各个scope的static的调用
        //下面开始输出函数
        code.byte(0xE9)
        code.dword(0)
        val offsetFuncStart=code.size()
        for(i in allScope){
//            writeCodeAsFunc(i.func_const,sections)
            for(i2 in i.function){//自定义函数提前输出
                writeFunBlock(i2,sections)
            }
            if(i.func_const.hasCode())
                writeFunBlock(i.func_const,sections)
            if(i.func_static.hasCode())
                writeFunBlock(i.func_static,sections)
            if(i.func_init.hasCode())
                writeFunBlock(i.func_init,sections)

        }
        code.wantFix("",4,code.size()-offsetFuncStart,0,code,offsetFuncStart-4)
        //下面开始调用静态块
        for(i in allScope){
            if(i.func_const.hasCode()){
                code.byte(0xE8)//call
                code.wantFix("",4,i.func_const.offset-code.size()-4,0,code,code.size())
                code.dword(0)
            }
        }
        for(i in allScope){
            if(i.func_static.hasCode()){
                code.byte(0xE8)//call
                code.wantFix("",4,i.func_static.offset-code.size()-4,0,code,code.size())
                code.dword(0)
            }
        }




//        for(i in scope.variable){
//            i.offset=data.size()
//            if (i.type?.name == "string") {
//                val cur=i.value
//                if(cur is variable_string) {
//                    data.wantFix("", 4, cur.value.offset, 3, data, data.size())
//                }else throw Exception("err")
//                data.dword(0)
//            } else if (i.type?.name == "dword") {
//                if (i.value!=null) {
//                    val cur=i.value
//                    if (cur is variable_int) {
//                        data.dword(cur.value.value)
//                    }else if (cur is variable_null) {
//                        data.dword(0)
//                    } else throw Exception("err")
//                } else {
//                    val cur=i.type
//                    if(cur!=null)
//                        data.put(cur.size, 0)
//                    else throw Exception("err")
//                }
//            } else {
//                val cur=i.type
//                if(cur!=null)
//                    data.put(cur.size, 0)
//                else throw Exception("err")
//            }
//        }

//        parseCodeBlock(scope.codeBlock,sections)

//        for(i in scope.codeBlock.code){
//            if(i.command=="invoke3"){
//                code.put(2,0x15FF)
//                code.wantFix("",4,i.param[0].value.offset,3,importTable,code.size())
//                code.dword(0)
//            }else if(i.command=="mov11"){//mov eax,ebx
//                // var_dump(i.param);die;
//                if(i.param[0]=="eax"){
//                    if(i.param[1]=="ebx"){
//                        code.put(1,0x8B)//mov eax,ebx
//                        code.put(1,0xC3)
//                    }else throw Exception("err")
//                }else throw Exception("err")
//                // var_dump(i.param);die;
//            }else if(i.command=="mov12"){//mov eax,[handle]
//                // var_dump(i.param);die;
//                if(i.param[0]=="eax"){
//                    code.put(1,0xB8)//mov eax,
//                    code.dword(i.param[1])
//                }else throw Exception("err")
//                // var_dump(i.param);die;
//            }else if(i.command=="mov13"){//mov eax,[handle]
//                // var_dump(i.param);die;
//                if(i.param[0]=="eax"){
//                    code.put(1,0xA1)//mov eax,
//                    code.wantFix("",4,i.param[1].offset,3,[data],code.size())//[变量地址]
//                    code.dword(0)
//                }else throw Exception("err")
//                // var_dump(i.param);die;
//            }else if(i.command=="mov31"){
//                if(i.param[1]=="eax"){
//                    code.put(1,0x89)//push
//                    code.put(1,0x05)//eax
//                    code.wantFix("",4,i.param[0].offset,3,[data],code.size())//[变量地址]
//                    code.dword(0)
//                }else throw Exception("err")
//                // var_dump(i.param);die;
//            }else if(i.command=="push1"){
//                if(i.param[0]=="eax"){
//                    code.put(1,0x50)//push eax
//                }else throw Exception("err")
//                // var_dump(i.param);die;
//                // code.put(2,0x35FF);//push dword
//                // code.dword(0x00000000);//
//            }else if(i.command=="push2"){//常数
//                // code.dword(0xCCCCCCCC);
//
//                code.put(1,0x68)
//                code.dword(i.param[0])
//                // var_dump("push2",i.param[0]);
//                // var_dump(i.param[0]);die;
//            }else if(i.command=="push3"){
//                // var_dump(i.param);die;
//                code.put(2,0x35FF)//push dword
//                code.wantFix("",4,i.param[0].offset,3,[data],code.size())//
//                code.dword(0)
//            }else if(i.command=="add12"){
//                // var_dump(i.param);die;
//                if(i.param[0]=="eax"){
//                    code.put(1,0x81)//add eax,
//                    code.put(1,0xC0)
//                }else throw Exception("err")
//
//                code.dword(i.param[1])
//            }else if(i.command=="add11"){
//                // var_dump(i.param);die;
//                if(i.param[0]=="eax"){
//                    if(i.param[1]=="ebx"){
//                        code.put(1,0x03)
//                        code.put(1,0xC3)
//                    }else throw Exception("err")
//                }else throw Exception("err")
//            }else if(i.command=="sub11"){
//                // var_dump(i.param);die;
//                if(i.param[0]=="ebx"){
//                    if(i.param[1]=="eax"){
//                        code.put(1,0x2B)
//                        code.put(1,0xD8)
//                    }else throw Exception("err")
//                }else throw Exception("err")
//            }else if(i.command=="neg1"){
//                if(i.param[0]=="eax"){
//                    code.put(1,0xF7)
//                    code.put(1,0xD8)
//                }
//            }else throw Exception("i.command err")
//        }
    }
    fun writeCodeAsFunc(codeblock:codeBlock,sections:build_section){
        val data=sections.get(1)
        val code=sections.get(1)
        val importTable=sections.get(2)
        val offset=code.size()

//        return offset
    }
    fun PhysicalSizeOf(num:Int,sub:Int=0): Int {
        if(num==0)return 0
        var result=0
        for(i in 0..num+512-sub step 512)
            result=i
        return result
    }
    fun VirtualSizeOf(num:Int,sub:Int=0): Int {
        if(num==0)return 0
        for(i in 0x1000..0xFFFF000 step 0x1000)//268431360
            if(i>num-sub)
                return i
        throw Exception("err")
    }
    fun VirtualAddressOf(section:build_section): Int {//rva
        val parent=section.parent
        if(parent==null)return 0
        var addr=0x1000
        for(i in parent.children){
            if(i==section)return addr
            addr+=tryAlign(i.size(),0x1000)
        }
        return addr
    }
    fun tryAlign(size:Int,length:Int=512):Int{//尝试对齐 返回对齐后的大小
        if(size%length==0)return size
        return size-size%length+length
    }
    fun compile(ast:ArrayList<ast_object>): build_section {//编译开始
        parse(ast)
        val data=build_section()//通用build数据包
        build=data
        val DOSHeader=data.addSection("DOS")
        val DOSStub=data.addSection("DOSStub")
        win32_tool.OutputDOSHeader(DOSHeader)
        win32_tool.OutputDOSStub(DOSStub)
        val PEHeader=data.addSection("PEHeader")
        win32_tool.PEHeader(PEHeader)
        //到这里文件长度为0x178
        if(data.childAllSize()!=0x178)throw Exception("文件大小验证失败！")

        //SectionTable开始文件偏移地址为0x178
        val SectionTable=data.addSection("SectionTable")
        val sections=this.sections
        if(sections==null)throw Exception("err")
        win32_tool.SectionTable(SectionTable,sections)

        val SizeOfHeader=data.childAllSize()
        PEHeader.nowFix("SizeOfHeaders",4,SizeOfHeader)
        var SizeOfAllSectionsBeforeRaw=SizeOfHeader

        data.addChild(sections)
        var SizeOfAllSectionsBefore=0x1000
        var NumberOfSections=0
        for (i in sections.children) {
            val size=i.size()
            if(size==0)continue
            NumberOfSections++
            val name=i.name
            val sectionTableItem=SectionTable.getByName(name)
            sectionTableItem.nowFix(i.name+".VirtualSize",4,size)
            if(name==".idata"){
                PEHeader.nowFix("ImportTable.Size",4,size)
            }
            val PhysicalSize = PhysicalSizeOf(size)
            val l=512-size%512
            for(i0 in 0 until l)
                i.put(1,0)
            if(name==".code"){
                PEHeader.nowFix("AddressOfEntryPoint",4,SizeOfAllSectionsBefore)
            }else if(name==".idata"){
                PEHeader.nowFix("ImportTable.Entry",4,SizeOfAllSectionsBefore)
            }

            sectionTableItem.nowFix(name+".VirtualAddress", 4,SizeOfAllSectionsBefore)
            SizeOfAllSectionsBefore += VirtualSizeOf(i.size())
            sectionTableItem.nowFix(name+".PointerToRawData", 4,SizeOfAllSectionsBeforeRaw)
            SizeOfAllSectionsBeforeRaw += PhysicalSize
            sectionTableItem.nowFix(name+".SizeOfRawData", 4,PhysicalSize)

            if(PhysicalSize%4096==0)SizeOfAllSectionsBefore-=4096

        }
        PEHeader.nowFix("NumberOfSections",2,NumberOfSections)
        PEHeader.nowFix("SizeOfImage",4,SizeOfAllSectionsBefore)
        //下面开始修复然后build

        data.doFix()
        data.build()
        return data
    }
    fun getRegister(str:String): asm_register? {//标识名称是否寄存器
        return when(str){
            "eax"-> asm_register_eax()
            "edx"-> asm_register_edx()
            else -> null
        }
//        when (str) {
//            "eax", "ebx", "ecx", "edx" -> return 4
//            "ax", "bx", "cx", "dx" -> return 2
//            "ah", "al", "bh", "bl", "ch", "cl", "dh", "dl" -> return 1
//        }
//        return 0//不是寄存器名称
    }
    fun getCommand(str:String): Int {//标识符是否为命令
        when (str) {
            "mov", "xor", "add" -> return 2
            "invoke", "push" -> return 1
            "nop" -> return 0
        }
        throw Exception("win32: unknown command!")
    }
}
