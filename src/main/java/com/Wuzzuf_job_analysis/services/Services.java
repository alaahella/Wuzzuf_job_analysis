package com.Wuzzuf_job_analysis.services;

import com.Wuzzuf_job_analysis.Controller.JobsPojo;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.evaluation.ClusteringEvaluator;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import static org.apache.spark.sql.functions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
public class Services implements ServiceInterface {

    @Autowired
    SparkSession sparkSession;

    private Dataset<Row> dataset;


    @Override
    public void readData(){
        // Load Data From CSV
        // and Remove duplicates and null values
        String path = "src/main/resources/Wuzzuf_Jobs.csv";
        dataset = sparkSession.read().option("header", true).csv(path).dropDuplicates().na().drop();
    }

    @Override
    public List<String> getStructure(){
        // get data schema and remove the first line (root)
        return Arrays.asList(dataset.schema().treeString().substring(5).split("\n"));
    }

    @Override
    public List<Row> getSummary(){
        return dataset.summary().collectAsList();
    }

    @Override
    public List<JobsPojo> getData(){
        // Return all data as list<Pojo>
        return dataset.as(Encoders.bean(JobsPojo.class)).collectAsList();
    }

    @Override
    public List<Row> jobCount(){
        // Sort data by company and get the first 10 records as a list
        List<Row> companiesJobCount = dataset.groupBy("Company").count()
                .sort(desc("count")).limit(10).collectAsList();
        return companiesJobCount;
    }

    @Override
    public List<Row> getPopularTitles(){
        List<Row> popularJobTitles = dataset.groupBy("Title").count()
                .sort(desc("count")).limit(10).collectAsList();

        return popularJobTitles;
    }

    @Override
    public List<List<?>> drawChart(String colName){
        Dataset<Row> companiesCount = dataset.groupBy(colName).count().sort(desc("count")).limit(10);
        List<String> companies = companiesCount.select(colName)
                .map((MapFunction<Row, String>) row -> row.<String>getAs(colName), Encoders.STRING())
                .collectAsList();
        List<Long> count = companiesCount.select("count")
                .map((MapFunction<Row, Long>) row -> row.<Long>getAs("count"), Encoders.LONG())
                .collectAsList();
        List<List<? extends Object>> list = new ArrayList<>();
        list.add(companies);
        list.add(count);
        return list;
    }

    @Override
    public List<Row> getPopularAreas(){
        List<Row> popularAreas = dataset.groupBy("Location").count()
                .sort(desc("count")).limit(10).collectAsList();

        return popularAreas;
    }

    @Override
    public List<Row> getPopularSkills(){
        List<Row> popularAreas = dataset.select("Skills")
                .map((MapFunction<Row, String>) row -> row.getAs("Skills"), Encoders.STRING())
                .flatMap((FlatMapFunction<String, String>) row -> Arrays.asList(row.split(",")).iterator(), Encoders.STRING())
                .groupBy("value").count().sort(desc("count")).limit(100).collectAsList();

    return popularAreas;
    }

    @Override
    public List<Row> factorize(String colName){
        StringIndexer indexer = new StringIndexer()
                .setInputCol(colName)
                .setOutputCol(colName + "Indexed");


        //Dataset<Row> yearsExp = Data.select("YearsExp");
        Dataset<Row> indexed = indexer.fit(dataset).transform(dataset);
        indexed.show();
        List<Row> indexedList = indexed.collectAsList();
        return indexedList;
    }


    @Override
    public void kMeans(){
        // Trains a k-means model.
        Dataset<Row> dataSelected = dataset.select("Title", "Company");


        String INDEX_APPENDIX = "_IDX";
        ArrayList<PipelineStage> stages = new ArrayList<>();

        for (String column : Arrays.asList("Title", "Company" )) {
            stages.add(new StringIndexer().setInputCol(column).setOutputCol(column + INDEX_APPENDIX));
//            stages.add(new OneHotEncoder().setInputCol(column + INDEX_APPENDIX).setOutputCol(column +
//                    VECTOR_APPENDIX));
        }

        Pipeline pipeline = new Pipeline()
                .setStages(stages.toArray(new PipelineStage[stages.size()]));

        Dataset<Row> processedDf = pipeline.fit(dataSelected).transform(dataSelected);
        processedDf = processedDf.select("Title" + INDEX_APPENDIX, "Company" + INDEX_APPENDIX);

        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"Title" + INDEX_APPENDIX, "Company" + INDEX_APPENDIX})
                .setOutputCol("features");
        Dataset<Row> output = assembler.transform(processedDf);
        output.show();


        KMeans kmeans = new KMeans().setK(2).setSeed(1L);
        KMeansModel model = kmeans.fit(output);
//
        // Make predictions
        Dataset<Row> predictions = model.transform(output);
//
//        // Evaluate clustering by computing Silhouette score
        ClusteringEvaluator evaluator = new ClusteringEvaluator();
//
        double silhouette = evaluator.evaluate(predictions);
        System.out.println("Silhouette with squared euclidean distance = " + silhouette);
//
        // Shows the result.
        Vector[] centers = model.clusterCenters();
        System.out.println("Cluster Centers: ");
        for (Vector center: centers) {
            System.out.println(center);
        }

    }

}
