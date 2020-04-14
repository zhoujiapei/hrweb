package com.mdl.zhaopin.service.impl;

import com.mdl.zhaopin.DTO.ResumeBaseDTO;
import com.mdl.zhaopin.handler.hanlp.parse.StrategyParseFile;
import com.mdl.zhaopin.service.ParseResumeService;
import com.mdl.zhaopin.handler.hanlp.parse.HanlpPraseResume;
import com.mdl.zhaopin.handler.hanlp.parse.TurnHtmlToText;
import com.mdl.zhaopin.handler.hanlp.parse.TurnPdfToText;
import com.mdl.zhaopin.handler.hanlp.parse.TurnTxtToText;
import com.mdl.zhaopin.handler.hanlp.parse.TurnWordToText;
import com.mdl.zhaopin.utils.CheckFileType;
import com.mdl.zhaopin.utils.JsonTools;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ParseResumeServiceImpl implements ParseResumeService {

    @Override
    public ResumeBaseDTO getResumeInfo(String filePath) throws IOException {

        //检查文件的类型
        String fileType = CheckFileType.getFileType(filePath);

        //下面使用策略模式，对不同格式的文件提取文本内容
        StrategyParseFile strategyParseFile = new StrategyParseFile();
        if (fileType != null) {
            if (fileType.endsWith("htm") || fileType.endsWith("html")) {
                strategyParseFile.setParseFileStrategy(new TurnHtmlToText(filePath));
            } else if (fileType.endsWith("wps") || fileType.endsWith("doc") || filePath.endsWith("docx")) {
                strategyParseFile.setParseFileStrategy(new TurnWordToText(filePath));
            } else if (fileType.endsWith("pdf")) {
                strategyParseFile.setParseFileStrategy(new TurnPdfToText(filePath));
            } else if (fileType.endsWith("txt")) {
                strategyParseFile.setParseFileStrategy(new TurnTxtToText(filePath));
            }
        }

        //使用nlp解析方式对文件进行解析
        HanlpPraseResume hanlpPraseResume = strategyParseFile.readFile().separateWords();
        int age = hanlpPraseResume.getAge();
        String name = hanlpPraseResume.getName();
        String email = hanlpPraseResume.getEmail();
        String sex = hanlpPraseResume.getSex();
        String phone = hanlpPraseResume.getPhone();
        String university = hanlpPraseResume.getUniversity();
        String address = hanlpPraseResume.getAddress();
        String profession = hanlpPraseResume.getProfession();
        String specialized = hanlpPraseResume.getSpecialized();
        String degree = hanlpPraseResume.getDegree();

        String workLength = hanlpPraseResume.getWorkLength();
        List<String> keyword = hanlpPraseResume.getKeyword();
        List<String> projectList = hanlpPraseResume.getProjectList();

        ResumeBaseDTO resume = new ResumeBaseDTO();
        resume.setName(name);
        resume.setSex(sex);
        resume.setAge(age);
        resume.setWorkLength(workLength);
        resume.setPhone(phone);
        resume.setEmail(email);
        resume.setDegree(degree);
        resume.setUniversity(university);
        resume.setExpectPlace(address);
        resume.setMajor(specialized);
        resume.setProfession(profession);

        String resumeStr = JsonTools.obj2String(resume);
        System.out.println(resumeStr + "/n");
        return resume;

    }

}
