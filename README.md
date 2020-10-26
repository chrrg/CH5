<p align="center">跨平台的CH5语言编译器</p>
<p align="center">可将用户输入的源码编译为机器码</p>
<p align="center">真正的强类型编译型，非解释型、虚拟机、LLVM</p>
# 关于本项目
本项目由本人独立编写  
项目最开始使用PHP进行编写  
后使用Kotlin进行重构  
暂时仅实现了Windows下的编译功能  
生成的最终产物为一个exe文件  
很多功能还不完善 可供学习使用  

# 目前支持的功能
```
CH5语言的Win32下静态编译器  
编译成exe文件  
调用Win32系统Api
表达式计算
表达式优先级
定义、赋值、修改变量
定义作用域和嵌套作用域
if语句
for语句
while语句
class定义和嵌套
func定义
比较运算符 = != > >= < <=
逻辑运算符 & && | ||
函数入参
函数调用
函数返回
```

# CH5语言语法

## 语法设计思路
能省则省  
简单明了  

## 语言风格
类似C、Java、Kotlin、Go等语法

## 语言特性
return的简写形式：
```
func caculate{
  =5*10
}
```
链式调用语法：
```
import printf from "msvcrt.dll"
func add10 num dword{=num+10}
func x10 num dword{=num*10}
func print str dword{str,"%s\n" printf}
100 add10 x10 add10 print
```
函数调用：
```
import system from "msvcrt.dll"
import ExitProcess from "KERNEL32.DLL:ExitProcess"
func getNum{=100}
func pause{"pause" system}
func end{0 ExitProcess}
.getNum
.pause
.end
```



