package cn.ch.ch5

open class ast_object(var token:Token)
class ast_binary(var operator:operateSymbol,var left:ast_object?,var right:ast_object?,token:Token):ast_object(token)//双目运算符
class ast_unary(var operator:operateSymbol,var left:ast_object?,var right:ast_object?,token:Token):ast_object(token)//单目运算符

class ast_nodeString(var value: String, token:Token):ast_object(token)//字符串常量节点
class ast_nodeInt(var value: Int, token:Token):ast_object(token)//整数常量节点
class ast_nodeDouble(var value:Double, token:Token):ast_object(token)//小数常量节点
class ast_nodeWord(var value:String, token:Token):ast_object(token)//单词节点
class ast_fun(var param:ArrayList<ast_fun_param>,var block:ast_block,token:Token):ast_object(token)//匿名函数
class ast_call(var name:ast_object,var value:ast_object,token:Token):ast_object(token)//链式调用
class ast_import(var name:String,var alias:String,var path:String,token:Token):ast_object(token)//匿名函数
class ast_var(var list:ArrayList<ast_var_item>,token:Token):ast_object(token)//定义变量
class ast_if(var expr:ast_object?,var trueBlock:ast_block?,var falseBlock:ast_block?,token:Token):ast_object(token)//判断语句
class ast_for(var expr:ast_block?,var body:ast_block?,token:Token):ast_object(token)//循环语句
class ast_while(var expr:ast_object?,var body:ast_block?,token:Token):ast_object(token)//循环语句
class ast_interface(var name:String,var body:ast_block?,token:Token):ast_object(token)//interface
class ast_class(var name:String,var general:ch5_type?,var extends:String?,var body:ast_block?,token:Token):ast_object(token)//class
class ast_func(var name:String,var param:ArrayList<ast_fun_param>,var block:ast_block?,token:Token):ast_object(token)//func
class ast_index(var expr:ast_object?,var value:ast_block?,token:Token):ast_object(token)//[]
class ast_use(var path: String,token:Token):ast_object(token)//use

class ast_var_item(var name:String,var type:ch5_type?,var value:ast_object?)
class ast_fun_param(var name:String,var type:ch5_type?)
class ast_init(var arr:ArrayList<ast_object>,token:Token):ast_object(token)
class ast_static(var arr:ArrayList<ast_object>,token:Token):ast_object(token)
class ast_block(var arr:ArrayList<ast_object>,token:Token):ast_object(token)
class ch5_type(var ident:String,var array:Int=0,var general:ch5_type?=null)