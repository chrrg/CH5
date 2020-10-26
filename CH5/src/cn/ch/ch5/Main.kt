package cn.ch.ch5

import cn.ch.ch5.win32.win32
import java.io.*

fun ms():Long{return System.nanoTime()}
fun ms(t:Long): String{return ((System.nanoTime()-t).toDouble()/1000000).toString()}
fun compile(path:String,output:String){
    println("-----------开始编译---------")
    var startTime: Long
    val sb = StringBuilder()
    val inputFilePath=File(path).absolutePath
    val input: InputStream = BufferedInputStream(FileInputStream(inputFilePath))
    var len = 0
    val temp = ByteArray(1024)
    while (input.read(temp).also({ len = it }) != -1)sb.append(String(temp, 0, len))
    val code:String=sb.toString()
    startTime = ms()//分词开始
    val control=parseControl(code,0,false)
    val fra=codeBody()
    Tokenizer.pre1(fra,control,null)
    println("分词用时：" + ms(startTime) + "毫秒")
//    println(Tokenizer.printCodeBody(fra))//打印分词器结果
    startTime = ms()//语法树解析开始
    val ast=AstParse.codeList(fra.arr)
//    AstParse.printAst(ast) //测试使用打印语法树
    println("语法树构建用时：" + ms(startTime) + "毫秒")
    startTime = ms()//编译开始
    val compiler=win32()
    try {
        val data = compiler.compile(ast)
        println("编译用时：" + ms(startTime) + "毫秒")
        startTime = ms()//输出文件
        try {
            data.outputFile(output)
        }catch(e:FileNotFoundException){
            println(e.message)
//            oldCode=""
        }
        println("文件输出用时：" + ms(startTime) + "毫秒")
    }catch(e:CompilerException){
        println("编译出错，位置：")
        val index=codeToLineColumn(code,e.token.start)
        val index2=codeToLineColumn(code,e.token.end)
        println("["+index[0]+":"+index[1]+"]-["+index2[0]+":"+index2[1]+"] "+e.text)
        e.printStackTrace()
    }

    println("-----------编译结束---------")
}
fun codeToLineColumn(code:String,value:Int):Array<Int>{
    val arr=code.split("\n")
    var index=0
    var line=0
    for(i in arr){
        line++
        if(index+i.length>=value){
            val column=index+value-index
            return arrayOf(line,column)
        }
        index+=i.length
    }
    return arrayOf(line,0)
}
fun help(){
    println("CH5 compiler-Help:\n" +
            "编译的java命令:\n" +
            "java -jar CH5.jar -c file.ch5 run.exe\n" +
            "按回车键进行编译的命令：\n" +
            "java -jar CH5.jar -cc file.ch5 run.exe")
}
fun main(command: Array<String>){

    var args=command
//    args=arrayOf("-cc","D:\\IdeaProjects\\CH5\\src\\cn\\ch\\ch5\\code.ch5","test.exe")//测试使用

    if(args.size==0){
        help()
        return
    }
    if(args[0]=="-c"){
        if(args.size>=3){
            try {
                compile(args[1], args[2])
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            println("命令缺少参数！")
        }
    }else if(args[0]=="-cc"){
        if(args.size>=3) {
            while (true) {
                println("开始编译中...")
                try {
                    compile(args[1], args[2])
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                println("按回车键重新编译.按Ctrl+C可停止")
                BufferedReader(InputStreamReader(System.`in`)).readLine()
            }
        }else{
            println("命令缺少参数！")
        }
    }else{
        println("unsupport command!")
    }
}