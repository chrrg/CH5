package cn.ch.ch5.win32

import java.io.DataOutputStream
import java.io.FileOutputStream

class build_section{
    var name:String=""
    var index:Int=0
    var byte: ArrayList<Byte>
    var parent: build_section?=null
    var children:ArrayList<build_section>
    var waitFix:ArrayList<waitFix_class>//等待修复
    var wantFix:ArrayList<wantFix_class>//想要修复
    init{
        byte=ArrayList()
        children=ArrayList()
        waitFix=ArrayList()
        wantFix=ArrayList()
    }
    fun build(){
        for(i in children){
            i.build()
            byte.addAll(i.byte)
        }
    }
    fun parent(){
        return parent()
    }
    fun size(): Int {
        return byte.size
    }
    fun childAllSize():Int{
        var size=0
        for(i in children)size+=i.childAllSize()+i.size()
        return size
    }
    fun addSection(name:String): build_section {
        val a= build_section()
        a.name=name
        a.index=children.size
        addChild(a)
        return a
    }
    fun getByName(str:String): build_section {
        for(i in children){
            if(i.name==str)return i
        }
        throw Exception("err!")
    }
    fun get(n:Int): build_section {
        return children[n]
    }
    fun addChild(child: build_section){
        children.add(child)
        child.parent=this
    }
    fun dword(value:Int){
        byte.add((value and 0x000000FF).toByte())
        byte.add((value and 0x0000FF00 shr 8).toByte())
        byte.add((value and 0x00FF0000 shr 16).toByte())
        byte.add((value and -0x1000000 shr 24).toByte())
    }
    fun word(value:Int){
        byte.add((value and 0x000000FF).toByte())
        byte.add((value and 0x0000FF00 shr 8).toByte())
    }
    fun byte(value:Int){
        byte.add((value and 0x000000FF).toByte())
    }
    fun put(size:Int,value:Int){
        when (size) {
            4 -> dword(value)
            2 -> word(value)
            1 -> byte(value)
            else -> throw Exception("compiler: size is not valid")
        }
    }
    fun set(offset:Int,size:Int,value:Int){
        when (size) {
            4 -> {
                byte[offset]=(value and 0x000000FF).toByte()
                byte[offset+1]=((value and 0x0000FF00 shr 8).toByte())
                byte[offset+2]=((value and 0x00FF0000 shr 16).toByte())
                byte[offset+3]=((value and -0x1000000 shr 24).toByte())
            }
            2 -> {
                byte[offset]=((value and 0x000000FF).toByte())
                byte[offset+1]=((value and 0x0000FF00 shr 8).toByte())
            }
            1 -> byte[offset]=((value and 0x000000FF).toByte())
            else -> throw Exception("compiler: size is not valid")
        }
    }
    fun wantFix(name:String, size:Int, value:Int, type:Int, param: build_section?, offset:Int=0){
        wantFix.add(wantFix_class(name, size, value, type, param, offset))
    }
    fun waitFix(name:String){//打点
        waitFix.add(waitFix_class(name, size()))
    }
    fun putSymbol(name:String,size:Int,value:Int){
        waitFix(name)
        put(size,value)
    }
    fun getWantFix(name:String): wantFix_class {
        for(i in wantFix){
            if(i.name==name){
                return i
            }
        }
        throw Exception("get err!")
    }
    fun nowFix(name:String,size:Int,value:Int){//立即修复
        for(i in waitFix){
            if(i.name==name){
                set(i.offset,size,value)
                waitFix.remove(i)
                return
            }
        }
        throw Exception("立刻修复失败！")
    }
    fun VirtualAddressOf(section: build_section?): Int {//rva
        if(section==null)return 0
        val parent=section.parent
        var addr=0x1000
        if(parent==null)return addr
        for(i in parent.children){
            if(i==section)return addr
            addr+=tryAlign(i.size(),0x1000)
        }
        return addr
    }
    fun tryAlign(size:Int,length:Int=512): Int {//尝试对齐 返回对齐后的大小
        if(size%length==0)return size
        return size-size%length+length
    }
    fun doFix(){//延迟修复
        for(i in children){
            i.doFix()
        }
        for(i2 in wantFix){
            if(i2.name==""){//使用offset偏移地址进行修复
                var value=i2.value
                if(i2.type and 0x1==0x1){//自定义的修复方式
                    value=VirtualAddressOf(i2.param)+value
                }
                if(i2.type and 0x2==0x2){
                    value+=0x400000//基址
                }
                set(i2.offset,i2.size,value)
//                wantFix.remove(i2)
                continue
            }
            var solved=false
            for(i in waitFix){
                if(i.name==i2.name){
                    var value=i2.value
                    if(i2.type and 0x1==0x1){
                        value=VirtualAddressOf(i2.param)+value
                    }
                    if(i2.type and 0x2==0x2){
                        value+=0x400000//基址
                    }
                    set(i.offset,i2.size,value)
//                    waitFix.remove(i)
//                    wantFix.remove(i2)
                    solved=true
                    break
                }
            }
            if(!solved)throw Exception("fix err: "+i2.name)
        }
        waitFix.clear()
        wantFix.clear()
    }
    fun outputFile(file:String){
        val bw = DataOutputStream(FileOutputStream(file))
        bw.write(byte.toByteArray())
        bw.close()
    }
}