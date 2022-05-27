package com.Wuzzuf_job_analysis.Controller;

import com.Wuzzuf_job_analysis.services.ServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@org.springframework.stereotype.Controller
public class Controller {

    @Autowired
    ServiceInterface service;
        
    @GetMapping("/")
    public String homePage(){
        service.readData();
        service.kMeans();
        return "index";
    }

    @GetMapping("/summary")
    public String showSummary(Model model){
        model.addAttribute("summary", service.getSummary());
        model.addAttribute("structure", service.getStructure());
        return "showSummary";
    }

    @GetMapping("/showData")
    public String showData(Model model){
        model.addAttribute("dataList", service.getData());
//        model.addAttribute("dataList", service.factorize("YearsExp"));

//        model.addAttribute("jobList", service.jobCount());

/*
        Dataset<Row> companiesCount = service.drawPieChart();
        List<String> companies = companiesCount.select("Company")
                .map((MapFunction<Row, String>) row -> row.<String>getAs("Company"), Encoders.STRING())
                .collectAsList();
        List<Long> count = companiesCount.select("count")
                .map((MapFunction<Row, Long>) row -> row.<Long>getAs("count"), Encoders.LONG())
                .collectAsList();
        model.addAttribute("companiesList", companies);
        model.addAttribute("countList", count);
*/
//        String msg = "Welcome to Thymeleaf Template";
//        // adding the attribute(key-value pair)
//        model.addAttribute("message", msg);
        return "showData";
//        List<List<?>> lists = service.drawPieChart();
//        model.addAttribute("companiesList", lists.get(0));
//        model.addAttribute("countList", lists.get(1));
//        return "pieChart";
//        return "index";

//        return Services.getData();

    }



    @GetMapping("/jobCount")
    public String getJobCount(Model model){
        model.addAttribute("jobList", service.jobCount());

        List<List<?>> lists = service.drawChart("Company");
        model.addAttribute("companiesList", lists.get(0));
        model.addAttribute("countList", lists.get(1));

        return "jobCount";
    }

    @GetMapping("/popularTitles")
    public String getPopularTitles(Model model){
        model.addAttribute("titleList", service.getPopularTitles());
        List<List<?>> lists = service.drawChart("Title");
        model.addAttribute("titlesList", lists.get(0));
        model.addAttribute("countList", lists.get(1));
        return "popularTitles";
    }

    @GetMapping("/popularAreas")
    public String getPopularAreas(Model model){
        model.addAttribute("areasList", service.getPopularAreas());
        List<List<?>> lists = service.drawChart("Location");
        model.addAttribute("locationList", lists.get(0));
        model.addAttribute("countList", lists.get(1));
        return "popularAreas";
    }

    @GetMapping("/popularSkills")
    public String getPopularSkills(Model model){
        model.addAttribute("skillsList", service.getPopularSkills());
        return "popularSkills";
    }

    @GetMapping("/factorize")
    public String showFactorizedData(Model model){
        model.addAttribute("indexedList", service.factorize("YearsExp"));

        return "factorizedData";
    }


}
