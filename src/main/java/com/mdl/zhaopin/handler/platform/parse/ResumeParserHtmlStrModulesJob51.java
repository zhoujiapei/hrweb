package com.mdl.zhaopin.handler.platform.parse;

import com.mdl.zhaopin.handler.platform.resume.Resume;
import com.mdl.zhaopin.handler.platform.resume.ResumeJob51;
import com.mdl.zhaopin.utils.JsonTools;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @Project : reusme-parse
 * @Package Name : com.mdl.zhaopin.handler.platform.parse
 * @Description : 针对通过插件在 51job 上获取的 搜索 的简历的html文件解析
 * @Author : xiekun
 * @Create Date : 2020年04月15日 15:36
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public class ResumeParserHtmlStrModulesJob51 extends AbstractResumeParser implements ResumeStrParser {

    @Override
    public String getName() {
        return "51Job";
    }

    @Override
    public boolean canParse(String file) {
        if (file == null) {
            return false;
        } else {
//            String name = file.getName();
//            return name.startsWith("51Job") && name.endsWith(".插件");
            return true;
        }
    }

    @Override
    public Resume parse(String resumeHtml) {

        ResumeJob51 resume = new ResumeJob51();

        //1、获取所有模块的html
        Map<String, String> moduleMap = getResumeMoudelMap(resumeHtml);
        System.out.println("moduleMap:" + JsonTools.obj2String(moduleMap));

        //2、各个模块进行接续获取数据
        parseResumeMap(moduleMap);

        return resume;
    }

    /**
     * 1、获取所有模块的html
     *
     * @param resumeHtml
     * @return
     */
    private Map<String, String> getResumeMoudelMap(String resumeHtml) {
        Map<String, String> moduleMap = new HashMap<>();

        //构建整个resumeHtml的Document对象
        Document doc = parse2Html(resumeHtml);

        //body > table > tbody > tr > td > table.bottom_border
        /** 最外层的所有tables **/
        Elements tables = doc.select("body > table > tbody > tr > td > table");

        if (tables != null) {
            //body > table > tbody > tr > td > table.bottom_border > tbody
            /** 第1层，基本信息姓名性别相关的table **/
            System.out.println("\n------------------------------------------------基本信息------------------------------------------------");
            Element table0 = tables.get(0);
            moduleMap.put("基本信息", table0.html());


            //------------------------------------------------个人信息------------------------------------------------
            /** 第2层，基本信息学历，学校等相关的table **/
            Element table1 = tables.get(1);
            moduleMap.put("个人信息", table1.html());


            /** 第3层，政治面貌等相关的table **/
            Element table2 = tables.get(2);
            if (table2 != null) {
                //#divInfo > td > table:nth-child(1) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1)
                Elements divInfo_tds = table2.getElementById("divInfo").select("td > table > tbody > tr > td.tba > table > tbody > tr");

                //------------------------------------------------求职意向------------------------------------------------
                moduleMap.put("求职意向", divInfo_tds.html());


                //------------------------------------------------自我评价------------------------------------------------
                Elements divInfo_tds_1 = divInfo_tds.select("td.tb1 td.txt1");
                moduleMap.put("自我评价", divInfo_tds_1.html());


                //------------------------------------------------工作经验------------------------------------------------
                //#divInfo > td > table:nth-child(3)
                Elements divInfo_tds_2 = table2.getElementById("divInfo").select("td > table:nth-child(3)");
                moduleMap.put("工作经验", divInfo_tds_2.html());


                //------------------------------------------------教育经历------------------------------------------------
                //#divInfo > td > table:nth-child(4)
                //#divInfo > td > table:nth-child(4) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1)
                Elements divInfo_tds_3 = table2.getElementById("divInfo").select("td > table:nth-child(4)");
                moduleMap.put("教育经历", divInfo_tds_3.html());

            }
        }
        return moduleMap;
    }


    /**
     * 2、各个模块进行接续获取数据
     *
     * @param map
     */
    public void parseResumeMap(Map<String, String> map) {

        String nameBaseInfoHtml = map.get("基本信息");
        System.out.println("\n------------------------------------------------基本信息------------------------------------------------");
        getNameBaseInfo(nameBaseInfoHtml);

        String introBaseInfoHtml = map.get("个人信息");
        System.out.println("\n------------------------------------------------个人信息------------------------------------------------");
        getIntroBaseInfo(introBaseInfoHtml);

        String jobHuntBaseInfoHtml = map.get("求职意向");
        System.out.println("\n------------------------------------------------求职意向------------------------------------------------");
        getJobHuntBaseInfo(jobHuntBaseInfoHtml);

        String selfEvalBaseInfoHtml = map.get("自我评价");
        System.out.println("\n------------------------------------------------自我评价------------------------------------------------");
        getSelfEvalBaseInfo(selfEvalBaseInfoHtml);

        String workExpBaseInfoHtml = map.get("工作经验");
        System.out.println("\n------------------------------------------------工作经验------------------------------------------------");
        getWorkExpBaseInfo(workExpBaseInfoHtml);

        String eduBaseInfoHtml = map.get("教育经历");
        System.out.println("\n------------------------------------------------教育经历------------------------------------------------");
        getEduBaseInfo(eduBaseInfoHtml);

    }


    /**
     * 6、教育经历
     *
     * @param eduBaseInfoHtml
     */
    private void getEduBaseInfo(String eduBaseInfoHtml) {

        Element divInfo_tds_3 = parse2Html(eduBaseInfoHtml);

        Elements divInfo_edu = divInfo_tds_3.select("tbody > tr:nth-child(2)");
        Elements select = divInfo_edu.select("td > table > tbody > tr > td.p15");

        for (Element element : select) {
            Elements deu = element.select("td[valign] , td.phd");
            String 在校时间 = deu.get(0).text().trim();
            String 学校名字 = deu.get(1).text().trim();
            String 学历 = deu.get(2).text().split("\\|")[0];
            String 在校专业 = deu.get(2).text().split("\\|")[1];

            System.out.println("\n在校时间：" + 在校时间 + "\n学校名字:" + 学校名字 + "\n学历:" + 学历 + "\n在校专业：" + 在校专业);
        }
    }

    /**
     * 5、工作经验
     *
     * @param workExpBaseInfoHtml
     */
    private void getWorkExpBaseInfo(String workExpBaseInfoHtml) {

        Element divInfo_tds_2 = parse2Html(workExpBaseInfoHtml);

        //#divInfo > td > table:nth-child(3) > tbody > tr:nth-child(2)
        Elements divInfo_work = divInfo_tds_2.select("tbody > tr:nth-child(2)");

        //#divInfo > td > table:nth-child(3) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1)
        Elements divInfo_worx_exps = divInfo_work.select("td > table > tbody > tr > td.p15");

        for (Element elements : divInfo_worx_exps) {
            //#divInfo > td > table:nth-child(3) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1) > td > table > tbody > tr:nth-child(1) > td.time

            Elements select = elements.select(" td > table > tbody > tr > td[valign] , td.rtbox");
            String 在职时间 = select.get(0).text().trim();
            String 公司名字 = select.get(1).text().trim();
            String 部门 = select.get(4).text().trim();
            String 岗位 = select.get(5).text().trim();
            String 工作描述 = select.get(7).text().trim();

            System.out.println("\n在职时间：" + 在职时间 + "\n公司名字:" + 公司名字 + "\n部门:" + 部门 + "\n岗位：" + 岗位 + "\n工作描述:" + 工作描述);

        }
    }


    /**
     * 4、自我评价
     *
     * @param selfEvalBaseInfoHtml
     */
    private void getSelfEvalBaseInfo(String selfEvalBaseInfoHtml) {

        Element divInfo_tds_1 = parse2Html(selfEvalBaseInfoHtml);

        String 自我评价 = divInfo_tds_1.text().trim();
        System.out.println("\n自我评价：" + 自我评价);
    }

    /**
     * 3、求职意向
     *
     * @param jobHuntBaseInfoHtml
     */
    private void getJobHuntBaseInfo(String jobHuntBaseInfoHtml) {

        Element divInfo_tds = parse2Html(jobHuntBaseInfoHtml);

        Elements divInfo_tds_0 = divInfo_tds.select("td.tb2 td.txt2");
        String 户口 = divInfo_tds_0.get(0).text().trim();
        String 身高 = divInfo_tds_0.get(1).text().trim();
        String 婚姻状况 = divInfo_tds_0.get(2).text().trim();
        String 家庭住址 = divInfo_tds_0.get(3).text().trim();
        String 政治面貌 = divInfo_tds_0.get(4).text().trim();
        String 期望薪资 = divInfo_tds_0.get(5).text().trim();
        String 地点 = divInfo_tds_0.get(6).text().trim();
        String 职位 = divInfo_tds_0.get(7).text().trim();
        String 到岗时间 = divInfo_tds_0.get(8).text().trim();
        String 工作类型 = divInfo_tds_0.get(9).text().trim();

        System.out.println("\n户口：" + 户口 + "\n身高:" + 身高 + "\n婚姻状况:" + 婚姻状况 +
                "\n家庭住址：" + 家庭住址 + "\n政治面貌:" + 政治面貌 + "\n期望薪资:" + 期望薪资 +
                "\n地点：" + 地点 + "\n职位:" + 职位 + "\n到岗时间:" + 到岗时间 + "\n工作类型:" + 工作类型);
    }


    /**
     * 2、个人信息
     *
     * @param introBaseInfoHtml
     */
    private void getIntroBaseInfo(String introBaseInfoHtml) {

        Element table1 = parse2Html(introBaseInfoHtml);

        if (table1 != null) {
            /** table1最外层 **/
            //body > table > tbody > tr > td > table.box2 > tbody > tr > td > table > tbody > tr > td:nth-child(1)
            /** 注意，最后面的选择器一定要用tr > td.tb2 ，不能是tr > td，否则会将tr下的所有td全部查出来 **/
            Elements table1_tds = table1.select("tbody > tr > td > table > tbody > tr > td.tb2");

            Element table1_td0_trs = table1_tds.get(0);
            if (table1_td0_trs != null) {
                Elements table1_td0_trs_tds = table1_td0_trs.select("table > tbody > tr");
                String 工作年限 = table1_td0_trs_tds.get(0).select("td > span").text().trim();
                String 职位 = table1_td0_trs_tds.get(1).select("td").get(1).text().trim();
                String 行业 = table1_td0_trs_tds.get(2).select("td").get(1).text().trim();
                System.out.println("\n最近工作年限：" + 工作年限 + "\n职位:" + 职位 + "\n行业:" + 行业);
            }

            Element table1_td1_trs = table1_tds.get(1);
            if (table1_td1_trs != null) {
                Elements table1_td1_trs_tds = table1_td1_trs.select("table > tbody > tr");
                String 专业 = table1_td1_trs_tds.get(1).select("td").get(1).text().trim();
                String 学校 = table1_td1_trs_tds.get(2).select("td").get(1).text().trim();
                String 学历 = table1_td1_trs_tds.get(3).select("td").get(1).text().trim();
                System.out.println("\n专业：" + 专业 + "\n学校:" + 学校 + "\n学历:" + 学历);
            }
        }
    }


    /**
     * 1、基本信息
     *
     * @param nameBaseInfoHtml
     */
    private void getNameBaseInfo(String nameBaseInfoHtml) {
        Element table0 = parse2Html(nameBaseInfoHtml);

        if (table0 != null) {
            //body > table > tbody > tr > td > table.bottom_border > tbody > tr > tds
            Elements table0_tbody0_tr0_tds = table0.select("tbody > tr > td");

            /** 姓名在职状况等信息 **/
            //body > table > tbody > tr > td > table.bottom_border > tbody > tr > td:nth-child(2)
            //基本信息的最外层
            Element table0_tbody0_tr0_td1 = table0_tbody0_tr0_tds.get(1);
            Elements table0_tbody0_tr0_td1_trs = table0_tbody0_tr0_td1.select("table > tbody > tr");

            if (table0_tbody0_tr0_td1_trs.size() > 0) {
                /** 孙超 **/
                //body > table > tbody > tr > td > table.bottom_border > tbody > tr > td:nth-child(2) > table > tbody > tr:nth-child(1)
                Element table0_tbody0_tr0_td1_tr0 = table0_tbody0_tr0_td1_trs.get(0);
                String tdseekname = table0_tbody0_tr0_td1_tr0.text().trim().split("和Ta聊聊")[0].trim();
                System.out.println("\n名字：" + tdseekname);

                //body > table > tbody > tr > td > table.bottom_border > tbody > tr > td:nth-child(2) > table > tbody > tr:nth-child(2)
                /** 目前正在找工作  13581583885  ww333ee@163.com  **/
                Element table0_tbody0_tr0_td1_tr1 = table0_tbody0_tr0_td1_trs.get(1);
                //body > table > tbody > tr > td > table.bottom_border > tbody > tr > td:nth-child(2) > table > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(2)
                Elements table0_tbody0_tr1_td = table0_tbody0_tr0_td1_tr1.select("td > table > tbody > tr > td");
                String 在职状况 = table0_tbody0_tr1_td.get(1).text().trim();
                String 手机号 = table0_tbody0_tr1_td.get(3).text().trim();
                String 邮箱 = table0_tbody0_tr1_td.get(5).text().trim();
                System.out.println("在职状况：" + 在职状况 + "\n手机号：" + 手机号 + "\n邮箱：" + 邮箱);

                //body > table > tbody > tr > td > table.bottom_border > tbody > tr > td:nth-child(2) > table > tbody > tr:nth-child(3)
                //body > table > tbody > tr > td > table.bottom_border > tbody > tr > td:nth-child(2) > table > tbody > tr:nth-child(3)
                /** 男 41岁（1978年7月1日） 现居住 北京 20年工作经验 **/
                Element table0_tbody0_tr0_td1_tr2 = table0_tbody0_tr0_td1_trs.get(3);
                Elements table0_tbody0_tr0_td1_tr2_tds = table0_tbody0_tr0_td1_tr2.select("td");
                String text = table0_tbody0_tr0_td1_tr2_tds.text().trim();
                String[] split = text.split("\\|");
                String 性别 = split[0].trim();
                String 年龄 = split[1].trim();
                String 住址 = split[2].replace("现居住", "").trim();
                String 工作经验 = split[3].trim();
                System.out.println("\n性别：" + 性别 + "\n年龄：" + 年龄 + "\n住址：" + 住址 + "\n工作经验：" + 工作经验);
            }
        }
    }


    /**
     * html转Document
     *
     * @param html
     * @return
     * @throws Exception
     */
    protected Document parse2Html(String html) {
        return Jsoup.parse(html);
    }


    public static void main(String[] args) throws Exception {
        String filePath = "/Users/admin/Desktop/简历解析/51job-插件-2.html";
        File file = new File(filePath);
        String html = FileUtils.readFileToString(file, "UTF-8");
        ResumeParserHtmlStrModulesJob51 resumeParser = new ResumeParserHtmlStrModulesJob51();
        ResumeJob51 resume = (ResumeJob51) resumeParser.parse(html);
//        System.out.println(JsonTools.obj2String(resume));
    }


}
