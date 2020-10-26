package cn.ch.ch5

open class operateSymbol(var symbol:String)
internal class op_add(symbol: String) : operateSymbol(symbol)//+
internal class op_minus(symbol: String) : operateSymbol(symbol)//-
internal class op_mutil(symbol: String) : operateSymbol(symbol)//*
internal class op_division(symbol: String) : operateSymbol(symbol)///
internal class op_mod(symbol: String) : operateSymbol(symbol)//%
internal class op_inc(symbol: String) : operateSymbol(symbol)//++
internal class op_dec(symbol: String) : operateSymbol(symbol)//--
internal class op_assign(symbol: String) : operateSymbol(symbol)//=
internal class op_rightAssign(symbol: String) : operateSymbol(symbol)//=>
internal class op_equal(symbol: String) : operateSymbol(symbol)//==
internal class op_notEqual(symbol: String) : operateSymbol(symbol)//!=
internal class op_left(symbol: String) : operateSymbol(symbol)//<<
internal class op_right(symbol: String) : operateSymbol(symbol)//>>
internal class op_comma(symbol: String) : operateSymbol(symbol)//,
internal class op_and(symbol: String) : operateSymbol(symbol)//&
internal class op_or(symbol: String) : operateSymbol(symbol)//|
internal class op_andAnd(symbol: String) : operateSymbol(symbol)//&&
internal class op_orOr(symbol: String) : operateSymbol(symbol)//||
internal class op_less(symbol: String) : operateSymbol(symbol)//<
internal class op_lessEqual(symbol: String) : operateSymbol(symbol)//<=
internal class op_greater(symbol: String) : operateSymbol(symbol)//>
internal class op_greaterEqual(symbol: String) : operateSymbol(symbol)//>=
internal class op_question(symbol: String) : operateSymbol(symbol)//?
internal class op_bit(symbol: String) : operateSymbol(symbol)//^
internal class op_at(symbol: String) : operateSymbol(symbol)//@
internal class op_pound(symbol: String) : operateSymbol(symbol)//#
internal class op_excl(symbol: String) : operateSymbol(symbol)//!
internal class op_dot(symbol: String) : operateSymbol(symbol)//.
internal class op_colon(symbol: String) : operateSymbol(symbol)//:
internal class op_backslash(symbol: String) : operateSymbol(symbol)//\


fun getSymbol(str:String): operateSymbol? {
    return when(str){
        "+"-> op_add(str)
        "-"-> op_minus(str)
        "*"-> op_mutil(str)
        "/"-> op_division(str)
        "%"-> op_mod(str)
        "++"-> op_inc(str)
        "--"-> op_dec(str)
        "="-> op_assign(str)
        "=>"-> op_rightAssign(str)
        "=="-> op_equal(str)
        "!="-> op_notEqual(str)
        "<<"-> op_left(str)
        ">>"-> op_right(str)
        ","-> op_comma(str)
        "."->op_dot(str)
        "&"-> op_and(str)
        "&&"-> op_andAnd(str)
        "|"-> op_or(str)
        "||"-> op_orOr(str)
        "<"-> op_less(str)
        ">"-> op_greater(str)
        "<="-> op_lessEqual(str)
        ">="-> op_greaterEqual(str)
        "?"-> op_question(str)
        "^"-> op_bit(str)
        "@"->op_at(str)
        "#"->op_pound(str)
        "!"->op_excl(str)
        ":"->op_colon(str)
        "\\"->op_backslash(str)
        else -> null
    }
}