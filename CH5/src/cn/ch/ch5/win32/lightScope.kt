package cn.ch.ch5.win32

class lightScope(var top: scope, var parent: lightScope?){
    var funBlock:fun_block?=null
    var variable:ArrayList<Variable>
    var children:ArrayList<lightScope>
    init{
        variable=ArrayList()
        children=ArrayList()
    }
    fun getSize(): Int {
        var size=0
        for(i in variable){
            val type=i.type
            if(type==null)throw Exception("err")
            size+=type.size
        }
        return size
    }
    fun getIdent(str:String): Variable? {
        for(i in variable){
            if(i.name==str)return i
        }
        return parent?.getIdent(str)
    }
    fun getChildSize():Int{
        var size=0
        size+=getSize()
        for(i in children)
            size+=i.getSize()
        return size
    }
    fun setVariableOffset(initSize:Int): Int {
        var size=initSize
        for(i in variable){
            i.offset=size
            size+=i.type!!.size
        }
        for(i in children){
            size=setVariableOffset(size)
        }
        return size
    }
}