package cn.ch.ch5

import java.util.*

open class Token constructor(var start:Int,var end:Int)
internal class Token_Word(var value:String,start: Int, end: Int) : Token(start, end)
internal class Token_String(var value:String,start: Int, end: Int) : Token(start, end)
internal class Token_Operator (var op:operateSymbol,start: Int, end: Int) : Token(start, end)
internal class Token_Int (var value:Int,start: Int, end: Int) : Token(start, end)
internal class Token_Double (var value:Double,start: Int, end: Int) : Token(start, end)
internal class Token_Crlf(start: Int, end: Int) : Token(start, end)
internal class Token_Bracket(var block:codeBody,start: Int, end: Int) : Token(start, end)
internal class Token_ArrayBracket(var block:codeBody,start: Int, end: Int) : Token(start, end)
internal class Token_Block(var block:codeBody,start: Int, end: Int) : Token(start, end)
class CompilerException(var text:String,var token:Token):Exception(text)
class parseControl(var code: String, var index: Int,var isCrlf:Boolean)
class codeBody {
    var arr: ArrayList<Token> = ArrayList()
    fun add(a: Token) {
        arr.add(a)
    }
}

object Tokenizer {

    fun printCodeBody(codeBody:codeBody,offset:Int=0):StringBuffer{
        val sp=StringBuffer()
        for (i in codeBody.arr){
            sp.append("\t".repeat(offset))
            sp.append("("+i.start+":"+i.end+")")
            sp.append(if(i is Token_Word){
                "[Token_Word]"+i.value
            }else if(i is Token_String){
                "[Token_String]"+i.value
            }else if(i is Token_Operator){
                "[Token_Operator]"+i.op.symbol
            }else if(i is Token_Int){
                "[Token_Int]"+i.value
            }else if(i is Token_Double){
                "[Token_Double]"+i.value
            }else if(i is Token_Crlf){
                "[Token_Crlf]"
            }else if(i is Token_Bracket){
                "[Token_Bracket] =>\n"+printCodeBody(i.block,offset+1)
            }else if(i is Token_ArrayBracket){
                "[Token_ArrayBracket] =>\n"+printCodeBody(i.block,offset+1)
            }else if(i is Token_Block){
                "[Token_Block] =>\n"+printCodeBody(i.block,offset+1)
            }else{
                "[unsupport]"
            })

            sp.append("\n")
        }
        return sp
    }
    fun isOperate(c: Char): Boolean {
        return getSymbol(c.toString())!=null
//        return when (c) {
//            '+', '-', '*', '/', '%', '!', '@', '#', '^', '&', '=', '|', ',', '.', '?', '\\', ':', '<', '>' -> true
//            else -> false
//        }
    }

    fun charType(c: Char): Int {
        return if (c == '(' || c == ')') {
            4
        } else if (c == '[' || c == ']') {
            5
        } else if (c == '{' || c == '}') {
            6
        } else if (isOperate(c)) {
            3
        } else if (c == '\"') {
            1 //字符串常量
        } else if (c == '\'') {
            2 //字符串常量
        } else if (c == ' ' || c == '\t') {
            7 //空白符
        } else if (c == '\r' || c == '\n' || c == ';') {
            8 //换行符
        } else {
            0 //词
        }
    }
    @Throws(Exception::class)
    fun pre1(fra: codeBody, controller:parseControl, charr: Char?) {
        val code=controller.code
        val len = code.length
        while(true) {
            if (controller.index >= len) {
                if (charr == null) {
                    return
                } else {
                    throw Exception("未期待的文件尾")
                }
            }
            val c=code[controller.index]
            if(c==')'||c==']'||c=='}'){
                if(c==charr)return
                throw Exception("未期待的符号"+c)
            }
            pre(fra,controller,charr)
        }
    }
    @Throws(Exception::class)
    fun pre(fra: codeBody, controller:parseControl, charr: Char?) {
        val code=controller.code
        var index=controller.index
        val len = code.length
        val isCrlf = controller.isCrlf

        if (index >= len) {
            if (charr==null) {
                return
            } else {
                throw Exception("未期待的文件尾")
            }
        }

        val index_start = index
        var c = code[index++]
        if (c == ' '||c=='\t'){
            controller.index=index;return
        }

        if(c=='\r'||c=='\n'||c==';'){
            controller.index=index
            if(isCrlf)return//如果前一个是换行就不添加换行了
            fra.add(Token_Crlf(index_start,index))
            controller.isCrlf=true
            return
        }
        controller.isCrlf=false
        when (charType(c)) {
            0 -> {
                if(c in '0'..'9'){//常数
                    var numberInt:Int=c.toInt()-48
                    var numberDouble =0.0
                    var isFloat=false
                    var floatPos=1.0
                    if(index>=len){
                        //文件尾就结束
                        if(charr==null){
                            fra.add(Token_Int(9,index_start,index))
                            controller.index=index
                            return
                        }else{
                            throw Exception("未期待的文件尾")
                        }
                    }

                    if(c=='0'){//0或16进制或普通数值
                        if(index<len&&code[index]=='x'){//16进制
                            index++
                            var hex=""
                            while(true){
                                if(index>=len)throw Exception("16进制未期待的文件尾")
                                val c2=code[index++]
                                if(c2 in '0'..'9'||c2 in 'a'..'f'||c2 in 'A'..'F'){
                                    hex+=c2
                                }else{
                                    if(c2 in 'g'..'z'||c2 in 'G'..'Z')throw Exception("16进制数未期待的字符:"+c2)
                                    break;
                                }
                            }
                            numberInt=Integer.parseInt(hex,16)
                            index--
                            fra.add(Token_Int(numberInt,index_start,index))
                            controller.index=index
                            return
                        }
                    }
                    while(true){
                        if(index>=len){
                            //文件尾就结束
                            if(charr==null){
                                if(isFloat){
                                    fra.add(Token_Double(numberDouble,index_start,index))
                                }else{
                                    fra.add(Token_Int(numberInt,index_start,index))
                                }
                                controller.index=index
                                return
                            }else{
                                throw Exception("未期待的文件尾")
                            }
                        }
                        c=code[index++]
                        if(c.toInt()>= 48 &&c.toInt()<= 57){
                            if(isFloat){
                                floatPos*=0.1
                                numberDouble += floatPos * (c.toInt() - 48)
                            }else{
                                numberInt=numberInt*10+c.toInt()-48
                            }
                        }else if(c.toInt()==46){//小数点
                            if(isFloat){
                                throw Exception("未期待的小数点")
                            }else{
                                numberDouble=numberInt.toDouble()
                                isFloat=true
                            }
                        }else if(charType(c)!=0){
                            //说明不是数值了
                            index--
                            if(isFloat){
                                fra.add(Token_Double(numberDouble,index_start,index))
                            }else{
                                fra.add(Token_Int(numberInt,index_start,index))
                            }
                            controller.index=index
                            return
                        }else{
                            if(isFloat && floatPos==1.0){
                                index-=2
                                fra.add(Token_Int(numberInt,index_start,index))
                                controller.index=index
                                return//不是词就结束
                            }
                            throw Exception("未期待的字符"+c)
                        }
                    }
                }
                var word:String= c.toString()
                while(true){
                    if(index>=len){
                        //文件尾就结束
                        if(charr==null){
                            fra.add(Token_Word(word,index_start,index))
                            controller.index=index
                            return
                        }else{
                            throw Exception("未期待的文件尾")
                        }
                    }
                    c=code[index++]
                    if(charType(c)!=0){
                        index--
                        fra.add(Token_Word(word,index_start,index))
                        controller.index=index
                        return//不是词就结束
                    }
                    word+=c
                }
            }
            1->{
                var result =""
                while(true){
                    if(index>=len)throw Exception("未关闭的常量字符串，未期待的文件尾")
                    c=code[index++]
                    if(c=='\\'){
                        c=code[index++]
                        when(c){
                            'r'->{
                                result+='\r'
                            }
                            'n'->{
                                result+='\n'
                            }
                            't'->{
                                result+='\t'
                            }
                            '0'->{
                                result+=0.toByte()
                            }
                            else->{
                                println("Warn: \\"+c+"不合法！")
                                result
                            }
                        }
                    }else if(c=='\"'){
                        //字符串关闭
                        fra.add(Token_String(result,index_start,index))
                        controller.index=index
                        return
                    }else{
                        result+=c
                    }
                }
            }
            2->{
                var result =""
                while(true) {
                    if (index >= len) throw Exception("未关闭的常量字符串，未期待的文件尾")
                    c = code[index++]
                    if (c == '\'') {
                        //字符串关闭
                        fra.add(Token_String(result, index_start, index))
                        controller.index=index
                        return
                    } else {
                        result += c
                    }
                }
            }
            3->{//操作符
                var result=c.toString()
                var symbol:operateSymbol= getSymbol(result)!!

                while(true){
                    if(index>=len){
                        //文件尾就结束
                        if(charr==null){
                            fra.add(Token_Operator(symbol,index_start,index))
                            controller.index=index
                            return
                        }else{
                            throw Exception("未期待的文件尾")
                        }
                    }
                    c=code[index++]
                    if(charType(c)!=3){
                        index--
                        fra.add(Token_Operator(symbol,index_start,index))
                        controller.index=index
                        return//不是符号就结束
                    }
                    result+=c
                    if(result=="//"){
//                        isCrlf=tempisCrlf
                        while(true){
                            if(index>=len){
                                if(charr==null){
                                    controller.index=index
                                    return
                                }else{
                                    throw Exception("未期待的文件尾")
                                }
                            }
                            c=code[index++]
                            if(c=='\r'||c=='\n'){
                                controller.index=index
                                if(isCrlf)return//如果前一个是换行就不添加换行了
                                fra.add(Token_Crlf(index_start,index))
                                controller.isCrlf=true
                                return
                            }
                        }
                    }else if(result=="/*"){
//                        isCrlf=tempisCrlf
                        while(true){
                            if(index>=len){
                                if(charr==null){
                                    controller.index=index
                                    return
                                }else{
                                    throw Exception("未期待的文件尾")
                                }
                            }
                            c=code[index++]
                            if(c=='*'){
                                if(index>=len){
                                    if(charr==null){
                                        controller.index=index
                                        return
                                    }else{
                                        throw Exception("未期待的文件尾")
                                    }
                                }
                                c=code[index++]
                                if(c=='/'){
                                    controller.index=index
                                    return
                                }
                            }
                        }
                    }
                    if(getSymbol(result)==null){
                        index--
                        fra.add(Token_Operator(symbol,index_start,index))
                        controller.index=index
                        return//不是符号就结束
                    }else{
                        symbol= getSymbol(result)!!

                    }

//                        if(temp=='&&'||temp=='||'){
//
//                        }else if(temp=='>>'||temp=='<<'||temp=='>>='||temp=='<<='){
//                        }else if(temp=='+='||temp=='-='||temp=='*='||temp=='/='||temp=='%='||temp=='&='||temp=='|='||temp=='^='){
//                            //赋值
//                        }else if(temp=='=>'||temp=='+=>'||temp=='-=>'||temp=='*=>'||temp=='/=>'||temp=='%=>'||temp=='&=>'||temp=='|=>'||temp=='^=>'){
//                            //右赋值
//                        }else if(temp=='++'||temp=='--'){
//
//                        }else if(temp=='>='||temp=='<='){
//
//                        }else if(temp=='=='){
//
//                        }else else{
//                            //不符合多符号规则就结束
//                            index--;
//                            fra.add(codeWord(3,result,index_start,index));
//                            goto a;
//                        }
//                        result=temp;

                }
            }
            4->{
                controller.index=index
                val subfra=codeBody()
                pre1(subfra,controller,')')
                fra.add(Token_Bracket(subfra,index_start,++controller.index))
                return
            }
            5->{
                controller.index=index
                val subfra=codeBody()
                pre1(subfra,controller,']')
                fra.add(Token_ArrayBracket(subfra,index_start,++controller.index))
                return
            }
            6->{
                controller.index=index
                val subfra=codeBody()
                pre1(subfra,controller,'}')
                fra.add(Token_Block(subfra,index_start,++controller.index))
                return
            }
        }
    }
}