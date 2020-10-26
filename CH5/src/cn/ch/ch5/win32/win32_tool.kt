package cn.ch.ch5.win32

object win32_tool{
    fun OutputDOSHeader(data:build_section){
        data.dword(0x805A4D);
        data.dword(0x1);
        data.dword(0x100004);
        data.dword(0xFFFF);
        data.dword(0x140);
        data.dword(0x0);
        data.dword(0x40);
        data.dword(0x0);
        data.dword(0x0);
        data.dword(0x0);
        data.dword(0x0);
        data.dword(0x0);
        data.dword(0x0);
        data.dword(0x0);
        data.dword(0x0);
        data.dword(0x80);//pe开始的文件偏移地址
    }
    fun OutputDOSStub(data:build_section){//可有可无
        data.dword(0xEBA1F0E);
        // var_dump(gettype(0xCD09B400));die;
        data.dword(0xCD09B400.toInt());
        data.dword(0x4C01B821);
        data.dword(0x687421CD);
        data.dword(0x70207369);
        data.dword(0x72676F72);
        data.dword(0x63206D61);
        data.dword(0x6F6E6E61);
        data.dword(0x65622074);
        data.dword(0x6E757220);
        data.dword(0x206E6920);
        data.dword(0x20534F44);
        data.dword(0x65646F6D);
        data.dword(0x240A0D2E);
        data.dword(0x0);
        data.dword(0x0);
    }
    fun PEHeader(data:build_section){//输出PE头
        //输出这里的时候section应该已经生成了
        data.dword(0x4550);                     //Signature = "PE"
        data.word(0x14C);                       //Machine 0x014C;i386
        data.putSymbol("NumberOfSections",2,0);
        // data.word(this.NumberOfSections());            //NumberOfSections = 4
        data.dword(0x0);                        //TimeDateStamp
        data.dword(0x0);                        //PointerToSymbolTable = 0
        data.dword(0x0);                        //NumberOfSymbols = 0
        data.word(0xE0);                        //SizeOfOptionalHeader
        // if(this.IsDLL)
        // data.word(0x210E);                      //Characteristics
        // else
        data.word(0x818F);                      //Characteristics

        data.word(0x10B);                       //Magic
        data.byte(0x5);                         //MajorLinkerVersion
        data.byte(0x0);                         //MinerLinkerVersion
        data.putSymbol("SizeOfCode",4,0);          //SizeOfCode
        data.putSymbol("SizeOfInitializedData",4,0);    //SizeOfInitializedData
        data.putSymbol("SizeOfUnInitializedData",4,0);  //SizeOfUnInitializedData
        data.putSymbol("AddressOfEntryPoint",4,0); //AddressOfEntryPoint
        data.putSymbol("BaseOfCode",4,0);          //BaseOfCode
        data.putSymbol("BaseOfData",4,0);          //BaseOfData
        data.dword(0x400000);                   //ImageBase 镜像基址
        data.dword(0x1000);                     //SectionAlignment 内存对齐大小
        data.dword(0x200);                      //FileAlignment 文件对齐大小
        data.word(0x1);                         //MajorOSVersion
        data.word(0x0);                         //MinorOSVersion
        data.word(0x0);                         //MajorImageVersion
        data.word(0x0);                         //MinorImageVersion
        data.word(0x4);                         //MajorSubSystemVerion
        data.word(0x0);                         //MinorSubSystemVerion
        data.dword(0x0);                        //Win32VersionValue
        data.putSymbol("SizeOfImage",4,0);         //SizeOfImage
        data.putSymbol("SizeOfHeaders",4,0);       //SizeOfHeaders
        data.dword(0x0);                        //CheckSum
        data.word(3);//AppType               //SubSystem = 2:GUI; 3:CUI
        data.word(0x0);                         //DllCharacteristics
        data.dword(0x10000);                    //SizeOfStackReserve
        data.dword(0x10000);                    //SizeOfStackCommit
        data.dword(0x10000);                    //SizeOfHeapReserve
        data.dword(0x0);                        //SizeOfHeapRCommit
        data.dword(0x0);                        //LoaderFlags
        data.dword(0x10);                       //NumberOfDataDirectories

        data.putSymbol("ExportTable.Entry",4,0);
        data.putSymbol("ExportTable.Size",4,0);

        data.putSymbol("ImportTable.Entry",4,0);
        data.putSymbol("ImportTable.Size",4,0);

        data.putSymbol("ResourceTable.Entry",4,0);
        data.putSymbol("ResourceTable.Size",4,0);

        data.dword(0x0); data.dword(0x0);      //Exception_Table
        data.dword(0x0); data.dword(0x0);      //Certificate_Table
        //data.putSymbol("ExceptionTable.Entry",4,0);
        //data.putSymbol("ExceptionTable.Size",4,0);
        //data.putSymbol("CertificateTable.Entry",4,0);
        //data.putSymbol("CertificateTable.Size",4,0);

        data.putSymbol("RelocationTable.Entry",4,0);
        data.putSymbol("RelocationTable.Size",4,0);

        data.dword(0x0); data.dword(0x0);      //Debug_Data
        data.dword(0x0); data.dword(0x0);      //Architecture
        data.dword(0x0); data.dword(0x0);      //Global_PTR
        data.dword(0x0); data.dword(0x0);      //TLS_Table
        data.dword(0x0); data.dword(0x0);      //Load_Config_Table
        data.dword(0x0); data.dword(0x0);      //BoundImportTable
        data.dword(0x0); data.dword(0x0);      //ImportAddressTable
        data.dword(0x0); data.dword(0x0);      //DelayImportDescriptor
        data.dword(0x0); data.dword(0x0);      //COMplusRuntimeHeader
        data.dword(0x0); data.dword(0x0);      //Reserved
    }

    fun createSection(root:build_section,name:String,Characteristics:Int): build_section {
        val data=root.addSection(name);
        val l=name.length
        for(i in 0 until l)
            data.byte(name[i].toInt())
        for(i in l until 8)
            data.byte(0);
        data.putSymbol(name+".VirtualSize",4,0);//data.VirtualSize
        data.putSymbol(name+".VirtualAddress",4,0);
        data.putSymbol(name+".SizeOfRawData",4,0);
        data.putSymbol(name+".PointerToRawData",4,0);
        data.putSymbol(name+".PointerToRelocations",4,0);
        data.dword(0x0);                        //PointerToLinenumbers
        data.word(0x0);                         //NumberOfRelocations
        data.word(0x0);                         //NumberOfLinenumbers
        data.dword(Characteristics);
        return data;
    }
    fun generateSections(root:build_section){
        root.addSection(".data");
        root.addSection(".code");
        root.addSection(".idata");
        root.addSection(".edata");
        root.addSection(".rsrc");
        root.addSection(".reloc");
    }
    fun SectionTable(data:build_section,sections:build_section){
        // data.addSection("data");
        // table_code.addSection("code");
        // table_idata.addSection("idata");
        // table_edata.addSection("edata");
        // table_rsrc.addSection("rsrc");
        // table_reloc.addSection("reloc");

        // if(sections)
        if(sections.get(0).size()>0)
            createSection(data,".data", (CH_INITIALIZED_DATA + CH_MEM_READ + CH_MEM_WRITE).toInt());
        if(sections.get(1).size()>0)
            createSection(data,".code",CH_CODE + CH_MEM_READ + CH_MEM_EXECUTE);
        if(sections.get(2).size()>0)
            createSection(data,".idata", (CH_INITIALIZED_DATA + CH_MEM_READ + CH_MEM_WRITE).toInt());
        if(sections.get(3).size()>0)
            createSection(data,".edata",CH_INITIALIZED_DATA + CH_MEM_READ);
        if(sections.get(4).size()>0)
            createSection(data,".rsrc",CH_INITIALIZED_DATA + CH_MEM_READ);
        if(sections.get(5).size()>0)
            createSection(data,".reloc",CH_MEM_DISCARDABLE + CH_INITIALIZED_DATA);
        val alignment=data.addSection("alignment");

        val l=512-data.parent!!.childAllSize()%512;
        for(i in 0 until l)alignment.byte(0);

        // data.wantFix("SizeOfHeaders",4,data.childAllSize());

        // fix512(data);//to do 应该修复整个区块的offset
        // var_dump(data.parent);die;
        // SizeOfHeader = ;
        // var_dump(data.size());

        // this.SizeOfAllSectionsBeforeRaw=this.SizeOfHeader;

    }
    fun fix512(root:build_section){
        val l=512-root.childAllSize()%512;
        for(i in 0 until l)root.byte(0);
    }
//    fun Section(data){
//        // data.
//    }
}