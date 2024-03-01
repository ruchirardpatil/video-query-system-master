package org.example.db;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.example.ElasticSearchClient;
import org.example.entities.ShotBoundaryFeatures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.util.Constants.DB_INDEX;

public class ES {

    public void store() throws IOException {
        try (RestHighLevelClient client = ElasticSearchClient.createClient()) {
            IndexRequest request = new IndexRequest("posts").id("1"); // "posts" is the name of the index
            String jsonString = "{" +
                    "\"user\":\"testuser\"," +
                    "\"message\":\"trying out Elasticsearch\"" +
                    "}";
            request.source(jsonString, XContentType.JSON);

            // execute index request
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            System.out.println(indexResponse.getId());
        }
    }

    public void search() throws IOException {
        try (RestHighLevelClient client = ElasticSearchClient.createClient()) {
            SearchRequest searchRequest = new SearchRequest("posts");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(searchResponse.toString());
        }
    }

    public void storeShot(ShotBoundaryFeatures shot) throws IOException {
        try (RestHighLevelClient client = ElasticSearchClient.createClient()) {
            String jsonStr = JSON.toJSONString(shot);
            IndexRequest request = new IndexRequest(DB_INDEX); // "posts" is the name of the index
            request.source(jsonStr, XContentType.JSON);

            // execute index request
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        }
    }

    public void getAllShots() throws IOException {
        try (RestHighLevelClient client = ElasticSearchClient.createClient()) {
            SearchRequest searchRequest = new SearchRequest(DB_INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(searchResponse.toString());
        }
    }

    public void emptyIndex() {
        try (RestHighLevelClient client = ElasticSearchClient.createClient()) {
            GetIndexRequest request = new GetIndexRequest(DB_INDEX);
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);

            if (!exists) {
                return;
            }

            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(DB_INDEX);
            client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);

            CreateIndexRequest createIndexRequest = new CreateIndexRequest(DB_INDEX);
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ShotBoundaryFeatures> searchFrames(ShotBoundaryFeatures targetShot) throws IOException {
        try (RestHighLevelClient client = ElasticSearchClient.createClient()) {
            Map<String, Object> params = new HashMap<>();
            params.put("targetR", targetShot.getR());
            params.put("targetG", targetShot.getG());
            params.put("targetB", targetShot.getB());
            params.put("targetEntropy", targetShot.getEntropy());

            var baseQuery = QueryBuilders.matchAllQuery();

            Script script = new Script(ScriptType.INLINE, "painless",
                    "double r = (doc['R'].size() == 0) ? 0 : doc['R'].value; " +
                            "double g = (doc['G'].size() == 0) ? 0 : doc['G'].value; " +
                            "double b = (doc['B'].size() == 0) ? 0 : doc['B'].value; " +
                            "double entropy = (doc['entropy'].size() == 0) ? 0 : doc['entropy'].value; " +
                            "Math.sqrt(Math.pow(params.targetR - r, 2) + Math.pow(params.targetG - g, 2) + Math.pow(params.targetB - b, 2) + Math.pow(params.targetEntropy - entropy, 2))",
                    params);

            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                    baseQuery,
                    ScoreFunctionBuilders.scriptFunction(script)
            ).boostMode(CombineFunction.MULTIPLY);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .query(functionScoreQueryBuilder)
                    .sort("_score", SortOrder.ASC)
                    .size(10);

            SearchRequest searchRequest = new SearchRequest(DB_INDEX);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            List<ShotBoundaryFeatures> res = new ArrayList<>();
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                System.out.println(hit.getSourceAsString());
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                ShotBoundaryFeatures shot = new ShotBoundaryFeatures(
                        (String) sourceAsMap.get("vidName"),
                        ((Number) sourceAsMap.get("frameNumber")).intValue(),
                        ((Number) sourceAsMap.get("R")).floatValue(),
                        ((Number) sourceAsMap.get("G")).floatValue(),
                        ((Number) sourceAsMap.get("R")).floatValue(),
                        ((Number) sourceAsMap.get("R")).floatValue(),
                        ((Number) sourceAsMap.get("timestamp")).longValue()
                );
                res.add(shot);
            }
            return res;
        }
    }
}
