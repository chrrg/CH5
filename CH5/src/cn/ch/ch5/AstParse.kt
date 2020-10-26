package cn.ch.ch5

import java.lang.Exception



class astParseControl(var arr:ArrayList<Token>,var index: Int, var priority: Int)

object AstParse {
        fun printAst(str:StringBuffer,ast:ast_var_item,offset:Int){
            str.append("\t".repeat(offset)+"name => "+ast.name+"\n")
            str.append("\t".repeat(offset)+"type => "+ast.type?.ident+"\n")
            printAst(str,ast.value,offset+1)
        }
        fun printFunParam(str:StringBuffer,ast:ArrayList<ast_fun_param>,offset:Int){
            for(i in ast){
                str.append("\t".repeat(offset)+"name => "+i.name+"\n")
                str.append("\t".repeat(offset)+"type => "+i.type?.ident+"\n")
            }
        }
        fun printAst(str:StringBuffer,ast:ast_object?,offset:Int){
            if(ast == null)
                str.append("\t".repeat(offset)+"NULL\n")
            else if(ast is ast_binary){
                str.append("\t".repeat(offset)+"[ast_binary]\n")
                str.append("\t".repeat(offset)+"operator => "+ast.operator.symbol+"\n")
                str.append("\t".repeat(offset)+"left => \n")
                printAst(str,ast.left,offset+1)
                str.append("\t".repeat(offset)+"right => \n")
                printAst(str,ast.right,offset+1)
            }else if(ast is ast_unary){
                str.append("\t".repeat(offset)+"[ast_binary]\n")
                str.append("\t".repeat(offset)+"operator => "+ast.operator.symbol+"\n")
                str.append("\t".repeat(offset)+"left => \n")
                printAst(str,ast.left,offset+1)
                str.append("\t".repeat(offset)+"right => \n")
                printAst(str,ast.right,offset+1)
            }else if(ast is ast_nodeString){
                str.append("\t".repeat(offset)+"[ast_nodeString]"+ast.value+"\n")
            }else if(ast is ast_nodeDouble){
                str.append("\t".repeat(offset)+"[ast_nodeDouble]"+ast.value+"\n")
            }else if(ast is ast_nodeWord){
                str.append("\t".repeat(offset)+"[ast_nodeWord]"+ast.value+"\n")
            }else if(ast is ast_nodeInt){
                str.append("\t".repeat(offset)+"[ast_nodeInt]"+ast.value+"\n")
            }else if(ast is ast_block){
                str.append("\t".repeat(offset)+"[ast_block]\n")
                str.append("\t".repeat(offset)+"block => \n")
                printAst(str,ast.arr,offset+1)
            }else if(ast is ast_fun){
                str.append("\t".repeat(offset)+"[ast_fun]\n")
                str.append("\t".repeat(offset)+"param => \n")
//                printAst(str,ast.param,offset+1)
                str.append("\t".repeat(offset)+"block => \n")
                printAst(str,ast.block,offset+1)
            }else if(ast is ast_init){
                str.append("\t".repeat(offset)+"[ast_init]\n")
                str.append("\t".repeat(offset)+"block => \n")
                printAst(str,ast.arr,offset+1)
            }else if(ast is ast_static){
                str.append("\t".repeat(offset)+"[ast_static]\n")
                str.append("\t".repeat(offset)+"block => \n")
                printAst(str,ast.arr,offset+1)
            }else if(ast is ast_call){
                str.append("\t".repeat(offset)+"[ast_call]\n")
                str.append("\t".repeat(offset)+"name => \n")
                printAst(str,ast.name,offset+1)
                str.append("\t".repeat(offset)+"value => \n")
                printAst(str,ast.value,offset+1)
            }else if(ast is ast_import){
                str.append("\t".repeat(offset)+"[ast_import]\n")
                str.append("\t".repeat(offset)+"name="+ast.name+"\n")
                str.append("\t".repeat(offset)+"alias="+ast.alias+"\n")
                str.append("\t".repeat(offset)+"path="+ast.path+"\n")
            }else if(ast is ast_var){
                str.append("\t".repeat(offset)+"[ast_var] => \n")
                for(i2 in ast.list){
                    printAst(str,i2,offset+1)
                }
//                str.append("\t".repeat(offset)+"name=>\n")
//                printAst(str,ast.,offset+1)
//                str.append("\t".repeat(offset)+"value=>\n")
//                printAst(str,ast.value,offset+1)
            }else if(ast is ast_func){
                str.append("\t".repeat(offset)+"[ast_func] =>\n")
                str.append("\t".repeat(offset)+"name = "+ast.name+"\n")
                str.append("\t".repeat(offset)+"param =>\n")
                printFunParam(str,ast.param,offset+1)
                str.append("\t".repeat(offset)+"block => \n")
                printAst(str,ast.block,offset+1)
            }else if(ast is ast_for){
                str.append("\t".repeat(offset)+"[ast_for]\n")
            }else if(ast is ast_while){
                str.append("\t".repeat(offset)+"[ast_while]\n")
                str.append("\t".repeat(offset)+"expr =>\n")
                printAst(str,ast.expr,offset+1)
                str.append("\t".repeat(offset)+"block =>\n")
                printAst(str,ast.body,offset+1)
            }else if(ast is ast_interface){
                str.append("\t".repeat(offset)+"[ast_interface]\n")
            }else if(ast is ast_class){
                str.append("\t".repeat(offset)+"[ast_class]\n")
            }else if(ast is ast_index){
                str.append("\t".repeat(offset)+"[ast_index]\n")
            }else if(ast is ast_if){
                str.append("\t".repeat(offset)+"[ast_if]\n")
                str.append("\t".repeat(offset)+"expr => \n")
                printAst(str,ast.expr,offset+1)
                str.append("\t".repeat(offset)+"true => \n")
                printAst(str,ast.trueBlock,offset+1)
                str.append("\t".repeat(offset)+"false => \n")
                printAst(str,ast.falseBlock,offset+1)

            }else{
                str.append("\t".repeat(offset)+"[unsupport]\n")
            }
        }
        fun printAst(str:StringBuffer,ast:ArrayList<ast_object>,offset:Int){
            for (i in ast){
                printAst(str,i,offset)
            }
        }
        fun printAst(ast:ArrayList<ast_object>){
            val str=StringBuffer()
            str.append("共有"+ast.size+"语句\n")
            printAst(str,ast,0)
            println(str)
        }
        fun codeList(arr: ArrayList<Token>):ArrayList<ast_object>{//code to ASTLIST
            var index=0
            val list=ArrayList<ast_object>()
            val control=astParseControl(arr,index,1000)
            while(true){
                if((Math.random()*1000000).toInt()==2)throw Exception("Error Processing 死循环或太复杂或太幸运")
                val node=codeAST(control)
                if(node!=null)list.add(node)//code to AST
                skip8(control)//跳过分号
                index=control.index
                if(index>=arr.size)return list
            }
        }
        fun codeAST(control:astParseControl):ast_object?{//arr:Int,index:Int=0,priority:Int=1000
            val arr=control.arr
//            var priority=control.priority
                var ast:ast_object?=null
                var priority2:Int
                while(true){
                    if((Math.random()*1000000).toInt()==2)throw Exception("Error Processing 死循环或太复杂或太幸运")
                if(control.index>=arr.size)return ast
                // var_dump(arr[control.index]);
                var cur=arr[control.index]
                if(cur is Token_Operator){//操作符
//                    if(operator==null)throw Exception("符号暂不支持！");
                    val opt=OperateType(cur)
                    if(opt==2){//双目运算符
                        // if(ast==null)throw Exception("暂不支持双目运算符缺失左值", 1);//-2可以看成0-2
                        priority2=getPriority(arr[control.index])//获取这个符号的优先级 4+ 3*
                        // if(priority2==14){}//说明现在是等于号
//                        println(control.priority)
//                        println(priority2)
                        if(control.priority==15)control.priority=100//等于号
                        if(control.priority==14)control.priority=2//说明前面一个是等于号，对等于号特殊处理，让等于号右目优先级为1
                        if(control.priority==99)control.priority=2//说明前面是一个链式调用
                        if(priority2>=control.priority)return ast

                        // var_dump(arr[control.index],priority2);
                        control.index++
                        if(priority2==15)priority2=1000//等于号的特殊处理
                        val tempPriority=control.priority
                        control.priority=priority2
                        ast=ast_binary(cur.op,ast,codeAST(control),cur)
                        control.priority=tempPriority
//                        ast=["type"=>arr[index-1].code,"left"=>ast,"right"=>codeAST(control)];
                        // var_dump(ast);die;
                        continue
                    }else if(opt==4){//处理++ 和 --
                        priority2=getPriority(arr[control.index])
                        if(control.priority==15)control.priority=100//等于号
                        if(control.priority==14)control.priority=2
                        if(priority2>=control.priority)return ast
                        control.index++
                        if(ast!=null){
                            ast=ast_unary(cur.op,ast,null,cur)
                        }else{
                            val tempPriority=control.priority
                            control.priority=priority2
                            ast=ast_unary(cur.op,null,codeAST(control),cur)
                            control.priority=tempPriority
                        }
                        continue
                    }else{
                        throw CompilerException("暂未实现的符号"+cur.op.symbol,cur)//不知道这个符号是什么类型
                    }
                }else if(cur is Token_Word){//链式调用支持
                    if(cur.value=="fun"){
                        val param=ArrayList<ast_fun_param>()//参数
                        while(true) {
                            control.index++
                            if (!skip8_1(control)) throw CompilerException("fun: unexpected end of line!",cur)
                            cur = arr[control.index]
                            if (cur is Token_Word) {
                                val name: String = cur.value//参数名
                                control.index++
                                if (!skip8_1(control)) throw CompilerException("fun: unexpected end of line!",cur)
                                val type_name = getType(control)
                                param.add(ast_fun_param(name, type_name))
                                if (!skip8_1(control)) throw CompilerException("fun: unexpected end of file!",cur)
                                cur = arr[control.index]
                                if (cur is Token_Operator && cur.op is op_comma)continue
                            }
                            break
                        }
                        cur=arr[control.index]
                        if(cur is Token_Block){
                            ast=ast_fun(param,ast_block(codeList(cur.block.arr),cur),cur)
                            control.index++
                            continue
                        }else throw CompilerException("fun: unsupport features, except code block",cur)
                    }else if(ast!=null){//说明前面有东西，这里就应该是链式调用
                        //保留关键字：if for while
                        priority2=getPriority(arr[control.index])
//                        println("priority2:"+priority2)
                        if(priority2>=control.priority)return ast
                        val tempPriority=control.priority
//                        println(control.priority)
                        control.priority=priority2
                        val ident=codeAST(control)
//                        println(control.priority)
                        control.priority=tempPriority
//                        println(control.priority)
                        ast=ast_call(ident!!,ast,cur)
                        continue
                    }else{
                        if(cur.value=="import"){//添加import支持
                            control.index++
                            if(!skip8_1(control))throw CompilerException("import: unexpected end of file!",cur)
//                            
                            var import_name=""
                            val import_alias: String
                            var import_path: String

                            cur=arr[control.index]
                            if(cur is Token_Word){
                                import_alias=cur.value//api的别名，又是编译器的常量名
                                control.index++
                                if(!skip8_1(control))throw CompilerException("import: unexpected end of file!",cur)
                                cur=arr[control.index]
                                if(cur is Token_Word&&cur.value=="from"){
                                    control.index++
                                    if(!skip8_1(control))throw CompilerException("import: unexpected end of file!",cur)
                                    cur=arr[control.index]
                                    if(cur is Token_String){//字符串
                                        import_path=cur.value
                                        val import_paths=import_path.split(":")
                                        if(import_paths.size==0)throw CompilerException("import: invalid path!",cur)
                                        if(import_paths.size==1){
                                            import_name=import_alias
                                        }else if(import_paths.size==2){
                                            import_name=import_paths[1]
                                        }else if(import_paths.size>2)throw CompilerException("import: invalid path!",cur)
                                        import_path=import_paths[0]
                                        if(import_path=="")throw CompilerException("import: invalid path!",cur)
                                        control.index++
                                        if(skip8_1(control))throw CompilerException("import: statement ended!",cur)
                                        ast=ast_import(import_name,import_alias,import_path,cur)
                                        return ast
                                    }else throw CompilerException("import: except string",cur)
                                }else throw CompilerException("import: except word 'from'",cur)
                            }else throw CompilerException("import: unsupport features",cur)


                        }else if(cur.value=="var"){//添加var支持
                            /*
                            var a byte
                            var b dword=0,b=0
                            */
                            val var_list=ArrayList<ast_var_item>()
                            ast=ast_var(var_list,cur)
                            while(true) {
                                control.index++
                                if (!skip8_1(control)) throw CompilerException("if: unexpected end of file!",cur)
                                cur = arr[control.index]
                                if (cur is Token_Word) {
                                    val var_name = cur.value//变量名
                                    var var_type: ch5_type? = null
                                    var var_value: ast_object? = null
                                    //                                var var_item=["name"=>var_name,"type"=>null,"value"=>null];
                                    control.index++
                                    if (!skip8_1(control)) {
                                        var_list.add(ast_var_item(var_name, var_type, var_value))
                                        return ast
                                    }
                                    cur = arr[control.index]
                                    if (cur is Token_Word) {//类型名
                                        var_type = getType(control)
                                        // var_dump(var_item["type"],arr[control.index]);die;
                                        if (!skip8_1(control)) {
                                            var_list.add(ast_var_item(var_name, var_type, var_value));return ast;
                                        }
                                        // var_dump(var_item["type"]);
                                        // var_item["type"]=arr[control.index].value;
                                        // control.index++;
                                        // if(!skip8_1(control)){ast["list"][]=var_item;return ast;}
                                    }
                                    // var_dump(var_item["type"],arr[control.index]);die;

                                    cur = arr[control.index]
                                    if (cur is Token_Operator) {
                                        if (cur.op is op_assign) {//=
                                            control.index++
                                            if (!skip8(control)) throw CompilerException("var: unexpected end of file!",cur)
                                            cur = arr[control.index]
                                            var_value = when (cur) {
                                                is Token_Word -> ast_nodeWord(cur.value, cur)
                                                is Token_Int -> ast_nodeInt(cur.value, cur)
                                                is Token_Double -> ast_nodeDouble(cur.value, cur)
                                                is Token_String -> ast_nodeString(cur.value, cur)
                                                else -> throw CompilerException("定义变量值仅允许常量值!",cur)
                                            }
                                            control.index++
                                            if (!skip8_1(control)) {
                                                var_list.add(ast_var_item(var_name, var_type, var_value));return ast;
                                            }
                                            cur = arr[control.index]
                                            if (cur is Token_Crlf) {
                                                var_list.add(ast_var_item(var_name, var_type, var_value))
                                                control.index++
                                                return ast
                                            } else if (cur is Token_Operator) {
                                                if (cur.op is op_comma) {
                                                    var_list.add(ast_var_item(var_name, var_type, var_value))
                                                    continue
                                                } else throw CompilerException("var: unsupport features, excepted dot symbol",cur)
                                            } else throw CompilerException("var: unsupport features, must be only value!",cur)//初始值只接受一个值 a+b需要括号
                                        } else if (cur.op is op_comma) {
                                            var_list.add(ast_var_item(var_name, var_type, var_value))
                                            continue
                                        } else throw CompilerException("var: unsupport features, excepted dot symbol,but except err",cur)
                                    }
                                    if (arr[control.index + 1] is Token_Crlf) {
                                        var_list.add(ast_var_item(var_name, var_type, var_value))
                                        control.index++
                                        return ast
                                    } else throw CompilerException("var: unsupport features",cur)
                                } else throw CompilerException("var: unsupport features",cur)
                            }
                        }else if(cur.value=="if"){//添加if支持
                            control.index++
                            if(!skip8(control))throw CompilerException("if: unexpected end of file!",cur)
                            val ifast=codeAST(control)
                            ast=ast_if(ifast,null,null,cur)
                            if(!skip8(control))throw CompilerException("if: unexpected end of file!",cur)
                            cur=arr[control.index]
                            if(cur is Token_Block){//大括号
                                ast.trueBlock=ast_block(codeList(cur.block.arr),cur)
                                control.index++
                                if(!skip8(control))return ast
                                cur=arr[control.index]
                                if(cur is Token_Word&&cur.value=="else"){
                                    control.index++
                                    if(!skip8(control))throw CompilerException("if: unexpected end of file!",cur)
                                    cur=arr[control.index]
                                    if(cur is Token_Block){//大括号
                                        val nextBlock=codeList(cur.block.arr)
                                        if(nextBlock.size>0) {
                                            ast.falseBlock = ast_block(nextBlock, cur)
                                        }
                                        control.index++
                                        return ast
                                    }else{
                                        val nextBlock=codeAST(control)
                                        if(nextBlock!=null){
                                            ast.falseBlock=ast_block(arrayListOf(nextBlock),cur)
                                        }
                                        return ast
                                    }
                                }else return ast
                            }else{
                                val nextBlock=codeAST(control)
                                if(nextBlock!=null) {
                                    ast.trueBlock = ast_block(arrayListOf(nextBlock),cur)
                                }
//                                ast["true"]=[codeAST(control)]
                                if(!skip8(control))return ast
                                cur=arr[control.index]
                                if(cur is Token_Word && cur.value=="else"){
                                    control.index++
                                    if(!skip8(control))throw CompilerException("if: unexpected end of file!",cur)
                                    cur=arr[control.index]
                                    if(cur is Token_Block){//大括号
                                        val nextBlock=codeList(cur.block.arr)
                                        ast.falseBlock=ast_block(nextBlock, cur)
//                                        nextBlock
//                                        ast["false"]=
                                        control.index++
                                        return ast
                                    }else{
                                        val nextBlock=codeAST(control)
                                        if(nextBlock!=null){
                                            ast.falseBlock=ast_block(arrayListOf(nextBlock),cur)
                                        }
                                        return ast
                                    }
                                }else return ast
                            }

                            //
                            // continue;
                            // var_dump("ifast",arr[control.index]);
                            // die;
                        }else if(cur.value=="for"){//添加for支持
                            control.index++
                            cur=arr[control.index]
                            ast=ast_for(null,null,cur)
                            if(cur is Token_Bracket){//小括号
                                val forexpr=codeList(cur.block.arr)
                                ast.expr=ast_block(forexpr,cur)
//                                ast=["type"=>"for","for"=>forexpr,"body"=>null,"desc"=>"for3 段式循环语句"]
                                control.index++
                                if(!skip8(control))return ast
                                cur=arr[control.index]
                                if(cur is Token_Block){//大括号
                                    ast.body=ast_block(codeList(cur.block.arr),cur)
                                    control.index++
                                }else{
                                    val nextBlock=codeAST(control)
                                    if(nextBlock!=null)
                                        ast.body=ast_block(arrayListOf(nextBlock),cur)
                                }
                                return ast
                            }else{
                                throw CompilerException("for: unsupport features,expect '('",cur)
                            }
                            // whileast=codeAST(control);
                            // ast=["type"=>"while","expr"=>whileast,"body"=>null,"desc"=>"while循环语句"];
                            // if(!skip8(control))return ast;
                            // // var_dump(arr[control.index]);
                            // cur=arr[control.index];if(cur is Token_Block){//大括号
                            // 	ast["body"]=codeAST(arr[control.index].value.arr);
                            // 	control.index++++;
                            // }else{
                            // 	ast["body"]=codeAST(control);
                            // }
                            // return ast;
                        }else if(cur.value=="while"){//添加while支持
                            control.index++
                            if(!skip8(control))throw CompilerException("while: unexpected end of file!",cur)
//                            whileast=ast_block(codeAST(control),cur)
                            ast=ast_while(codeAST(control),null,cur)
//                            ast=["type"=>"while","expr"=>whileast,"body"=>null,"desc"=>"while循环语句"]
                            if(!skip8(control))return ast
                            // var_dump(arr[control.index]);

                            cur=arr[control.index]
                            if(cur is Token_Block){//大括号
                                // var_dump(arr[control.index].value.arr);
                                val nextBlock=codeList(cur.block.arr)
                                if(nextBlock.size>0)
                                    ast.body=ast_block(nextBlock,cur)
                                control.index++
                            }else{
                                val nextBlock=codeAST(control)
                                if(nextBlock!=null)
                                    ast.body=ast_block(arrayListOf(nextBlock),cur)
                            }
                            return ast
                        }else if(cur.value=="interface"){//添加interface接口支持
                            control.index++
                            if(!skip8(control))throw CompilerException("interface: unexpected end of file!",cur)
                            cur=arr[control.index];
                            if(cur is Token_Word){
                                var name=cur.value
                                control.index++
                                if(!skip8(control))throw CompilerException("interface: unexpected end of file!",cur)
                                cur=arr[control.index];
                                ast=ast_interface(name,null,cur)
                                if(cur is Token_Block){
                                    val nextBlock=codeList(cur.block.arr)
                                    if(nextBlock.size>0)
                                        ast.body=ast_block(nextBlock,cur)
                                    control.index++
                                    return ast
                                }else throw CompilerException("interface: unsupport features",cur)
                            }else throw CompilerException("interface: unsupport features",cur)
                        }else if(cur.value=="class"){//添加class类支持

//                            throw Exception("class: unsupport features")
                            control.index++
                            if(!skip8(control))throw CompilerException("class: unexpected end of file!",cur)
                            cur=arr[control.index];
                            if(cur is Token_Word) {
                                val name = cur.value
                                control.index++
                                cur=arr[control.index];
                                if (cur is Token_Block) {
                                    ast=ast_class(name,null,null,null,cur)
                                    val nextBlock=codeList(cur.block.arr)
                                    if(nextBlock.size>0)
                                        ast.body=ast_block(nextBlock,cur)
//                                    ast = ["type"=>"class", "name"=>name, "general"=>param_T, "extends"=>extends, "block"=>codeList(arr[control.index].block.arr)]
                                    control.index++
                                    return ast
                                } else throw CompilerException("class: unsupport features",cur)
                            } else throw CompilerException("class: unsupport features",cur)
//                                extends=[]
//                                param_T=[]
//                                class_extends:
//                                control.index++
//                                if(!skip8(control))throw Exception("class: unexpected end of file!")
//                                cur=arr[control.index];
//                                if(cur is Token_Operator){
//                                    if(arr[control.index].value=="<"){
//                                        control.index++
//                                        if(!skip8(control))throw Exception("class: unexpected end of file!")
//                                        cur=arr[control.index];if(cur is Token_Word){
//                                            param_T[]=arr[control.index]
//                                            control.index++
//                                            if(!skip8(control))throw Exception("class: unexpected end of file!")
//                                            cur=arr[control.index];if(cur is Token_Operator){
//                                                if(arr[control.index].value==">"){
//                                                    goto class_extends
//                                                }else throw Exception("class: unsupport features")
//                                            }else throw Exception("class: unsupport features")
//                                        }else throw Exception("class: unsupport features")
//                                    }else if(arr[control.index].value=="+"){
//                                        control.index++
//                                        if(!skip8(control))throw Exception("class: unexpected end of file!")
//                                        cur=arr[control.index];if(cur is Token_Word){
//                                            extends[]=arr[control.index].value
//                                            goto class_extends
//                                        }else throw Exception("class: unsupport features")
//                                    }else throw Exception("class: unsupport features")
//                                }else cur=arr[control.index];
//                                if(cur is Token_Block){
//                                    ast=["type"=>"class","name"=>name,"general"=>param_T,"extends"=>extends,"block"=>codeList(arr[control.index].block.arr)]
//                                    control.index++
//                                    return ast
//                                }else throw Exception("class: unsupport features")
//                            }else throw Exception("class: unsupport features")
                        }else if(cur.value=="func"){
                            val param=ArrayList<ast_fun_param>()

                            control.index++
                            if(!skip8_1(control))throw CompilerException("func: unexpected end of line!",cur)
                            val func_name=getIdentDot(control)[0]
//                            if(func_name.size==0)throw Exception("func: must have a name")
                            while(true) {
                                if (!skip8_1(control)) throw CompilerException("func: unexpected end of line!",cur)
                                cur = arr[control.index];
                                if (cur is Token_Word) {
                                    val name = cur.value//参数名
                                    control.index++
                                    if (!skip8_1(control)) throw CompilerException("func: unexpected end of line!",cur)
                                    val type_name = getType(control)
                                    param.add(ast_fun_param(name, type_name))//["name"=>name,"type"=>type_name]
                                    cur = arr[control.index];
                                    if (cur is Token_Operator && cur.op is op_comma) {
                                        control.index++
                                        continue
                                    }
                                }
                                break;
                            }
                            cur=arr[control.index];
                            if(cur is Token_Block){
                                ast=ast_func(func_name,param,null,cur)
                                val nextBlock=codeList(cur.block.arr)
                                if(nextBlock.size>0)ast.block=ast_block(nextBlock,cur)
//                                ast=["type"=>"func","name"=>func_name,"param"=>param,"block"=>]
                                control.index++
                                return ast
                            }else throw CompilerException("func: unsupport features, except code block!",cur)
                        }else if(cur.value=="use"){//添加use支持

                            control.index++
                            if(!skip8_1(control))throw CompilerException("use: unexpected end of line!",cur)
                            cur=arr[control.index];
                            if(cur is Token_String){
                                ast=ast_use(cur.value,cur)
                                control.index++
                                return ast
                            }else throw CompilerException("func: unsupport features, except code block!",cur)
                        }else if(cur.value=="asm"){
//                            control.index++
//                            if(!skip8_1(control))throw Exception("use: unexpected end of line!")
//                            cur=arr[control.index];if(cur is Token_Block){
//                                ast=["type"=>"asm","block"=>asm::asmList(arr[control.index].value.arr)]
//                                control.index++
//                                return ast
//                            }else
                                throw CompilerException("use: unsupport features",cur)
                        }else if(cur.value=="init"){
                            control.index++
                            cur=arr[control.index];
                            if(cur is Token_Block){
                                control.index++
                                return ast_init(codeList(cur.block.arr),cur)
                            }else throw CompilerException("func: unsupport features, except code block!",cur)
                        }else if(cur.value=="static"){
                            control.index++
                            cur=arr[control.index];
                            if(cur is Token_Block){
                                control.index++
                                return ast_static(codeList(cur.block.arr),cur)
                            }else throw CompilerException("func: unsupport features, except code block!",cur)
                        }else{
                            ast=ast_nodeWord(cur.value,cur)
                            // throw Exception("ast: unsupport features ".arr[control.index].value);
                        }
//                        if(cur is Token_Word)

//                        else if(cur is Token_Int)
//                            ast=ast_nodeInt(cur.value,cur)
//                        else if(cur is Token_Double)
//                            ast=ast_nodeDouble(cur.value,cur)
//                        else if(cur is Token_String)
//                            ast=ast_nodeString(cur.value,cur)
//                        else
                    }
                }else if(cur is Token_Crlf||cur is Token_Block){
//                    control.index++
                    return ast
                }else if(cur is Token_Bracket){//小括号
                    val control2=astParseControl(cur.block.arr,0,1000)
                    val nextBlock=codeAST(control2)
                    if(nextBlock!=null)ast=ast_block(arrayListOf(nextBlock),cur)
                    else ast=null

                }else if(cur is Token_ArrayBracket){//中括号
                    priority2=getPriority(arr[control.index])
                    if(priority2>control.priority)return ast
                    val control2=astParseControl(cur.block.arr,0,1000)
                    val nextBlock=codeAST(control2)
                    control.index++

                    ast=ast_index(ast,null,cur)
//                    ast=["type"=>"[]","name"=>ast,"value"=>,"desc"=>"注释：中括号"]
                    if(nextBlock!=null){
                        ast.value=ast_block(arrayListOf(nextBlock),cur)
                    }
                    continue
                }else{
                    if(ast!=null)throw CompilerException("不允许丢弃",cur)
                    if(cur is Token_Int)
                        ast=ast_nodeInt(cur.value,cur)
                    else if(cur is Token_Double)
                        ast=ast_nodeDouble(cur.value,cur)
                    else if(cur is Token_String)
                        ast=ast_nodeString(cur.value,cur)
                }
                control.index++
            }
        }

        fun skip8(control: astParseControl):Boolean{//去掉多余分号 如果文件结尾就false
            while(true){
                if(control.index>=control.arr.size)return false
                if(control.arr[control.index] !is Token_Crlf)return true
                control.index++
                continue
            }
        }
        fun is8(control: astParseControl):Boolean{
            return control.index>=control.arr.size||control.arr.get(control.index) is Token_Crlf
        }
        fun skip8_1(control: astParseControl):Boolean{//文件结尾或分号就false
            if(control.index>=control.arr.size)return false
            if(control.arr[control.index] is Token_Crlf){control.index++;return false;}
            return true
        }
        fun OperateType(node:Token):Int{//双目运算符 单目运算符等
            if(node is Token_Operator){
                when (node.op.symbol){
                    "+", "-", "*", "/", "%", ".", ",", "=", "/=", "*=", "%=", "+=", "-=", "<<=", ">>=", "&=", "^=", "|=", "=>", "/=>", "*=>", "%=>", "+=>", "-=>", "<<=>", ">>=>", "&=>", "^=>", "|=>", "||", "&&", "|", "&", "==", "!=", "^", ">", "<", ">=", "<=", ">>", "<<" -> return 2 //双目运算符
                    "++", "--" -> return 4 //两边都可以的单目运算符
                    "!" -> return 5 //单目运算符仅右目
                    "?" -> return 3 //三目运算符
                }
                throw CompilerException("unsupport OperateType char",node)
            }else if(node is Token_Word){
                return 99//链式调用优先级99
            }else{
                throw CompilerException("unsupport OperateType char",node)
            }
        }
        fun getType(control:astParseControl):ch5_type?{//得到类型
            val ident=getIdentDot(control)
//            if(ident===null)return null;
            val type=ch5_type("")
            val arr=control.arr
            type.ident=ident[0]
            if(is8(control))return type
            var cur=arr[control.index]
            if(cur is Token_Operator && cur.op is op_less){//<
                while(true) {
                    control.index++
                    if (is8(control))
                        throw CompilerException("type: unexpected end of line!",cur)
                    val subtype = getType(control)
                    if (subtype == null) throw CompilerException("type: general type is invalid!",cur)
                    type.general = subtype//todo .add
                    cur = arr[control.index]
                    if (cur is Token_Operator) {
                        if (cur.op is op_comma) {//,
                            continue
                        } else if (cur.op is op_greater) {
                            control.index++
                            if (is8(control))
                                return type
                        } else throw CompilerException("type: except >,but got err",cur)
                    }
                    break;
                }
            }
            while(true) {
                cur = arr[control.index]
                if (cur is Token_ArrayBracket) {
                    type.array++
                    control.index++
                    if (is8(control)) return type
                    continue
                }
                break
            }
            return type
        }
        fun getIdentDot(control:astParseControl):ArrayList<String>{//得到链式的对象
            var list=ArrayList<String>()
            while(true) {
                var cur = control.arr[control.index]
                if (cur is Token_Word) {
                    list.add(cur.value)
                    control.index++
                    if (is8(control)) return list
                    cur = control.arr[control.index]
                    if (cur is Token_Operator && cur.op is op_dot) {
                        control.index++
                        if (is8(control)) throw CompilerException("Syntax Error!",cur)
                        continue
                    } else {
                        return list
                    }
                } else {
                    return list
                }
            }
        }
        fun getPriority(node:Token):Int{
            if(node is Token_ArrayBracket){//中括号 数组
                return 1
            }else if(node is Token_Word){//链式调用优先级
                return 99
            }else if(node is Token_Operator){
                return when (node.op.symbol) {
                    "," -> 16
                    "=", "/=", "*=", "%=", "+=", "-=", "<<=", ">>=", "&=", "^=", "|=" -> 15
                    "=>", "/=>", "*=>", "%=>", "+=>", "-=>", "<<=>", ">>=>", "&=>", "^=>", "|=>" -> 14
                    "?", ":" -> 13
                    "||" -> 12
                    "&&" -> 11
                    "|" -> 10
                    "^" -> 9
                    "&" -> 8
                    "==", "!=" -> 7
                    ">", ">=", "<", "<=" -> 6
                    "<<", ">>" -> 5
                    "+", "-" -> 4
                    "*", "/", "%" -> 3
                    "!" -> 2
                    ".", "++", "--" -> 1
                    else -> throw CompilerException("unsupport operator char:"+node.op.symbol,node)
                }
            }else{
                throw CompilerException("unsupport operator Priority!",node)
            }
        }
}