package searchengine.dto.statistics;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.Page;
import searchengine.model.SiteModel;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SitesRepository;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

//Класс из раздела мноргопоточность
//Рассмотреть варианты внедрения
public class ParseLinksTask extends RecursiveAction {
    @Autowired
    PageRepository pageRepository;
    private final String url;

    public ParseLinksTask(PageRepository pageRepository, String url) {
        this.pageRepository = pageRepository;
        this.url = url;
    }

    public ParseLinksTask(String url) {
        this.url = url;
    }

    //    public ParseLinksTask(final SiteModel siteModel) {
//        this.siteModel = siteModel;
//        this.url = siteModel.getUrl();
//    }
    public static volatile HashSet<String> allLinks = new HashSet<>();

    @Getter
    private static volatile HashMap<String, String> linksHashMap = new HashMap<>();
    @Getter

    private static  Connection.Response response;
    @Getter
    private static volatile List<Page> pagesList = new ArrayList<>();


    public TreeSet<String> getLinks(){

        TreeSet<String> treeLinks = new TreeSet<>();
        Document document = null;
        Elements elements = null;
        try {
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .referrer("http://www.google.com")
                    .timeout(20000)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true);
            Thread.sleep(250);
            response = connection.execute();

            document = connection.get();
            String content = document.body().html();


            addPageToRep(url, content, getHttpStatus());

            elements = document.body().select("a[href]");
           // System.out.println(elements.size()) ;
        } catch (HttpStatusException e) {
            e.printStackTrace();
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
        if(elements == null){
            return new TreeSet<>();
        }

        for (Element e : elements){
            String link = e.attr("abs:href");

            if(
                    link.endsWith("/")
                    && !allLinks.contains(link)
                    && link.startsWith(url) )
            {
                treeLinks.add(link);
            }
        }
        return treeLinks;
    }

    public int getHttpStatus(){
        int statusCode = response.statusCode();
        return statusCode;
    }

    private void addPageToRep(String url, String content, int httpStatus){
//        Page page = new Page(siteModel);
//        page.setPath(siteModel.getUrl());
//        page.setCode(1);
//        page.setContent("какойта кантент");
//        pageRepository.save(page);



           // page.setContent("some content");
            if(pagesList.stream().anyMatch(page -> cutPath(url).equals(page.getPath()))){

            } else
            {

                if (content == null) {
                    content = "";
                }
                Page page = new Page();
                page.setContent(content);
                page.setPath(cutPath(url));
                page.setCode(getHttpStatus());
                pagesList.add(page);
                //   pageRepository.save(page);

            }
    }


    @Override
    protected void compute() {

        allLinks.add(url);
        TreeSet<String> setFromOneLink;

        setFromOneLink = getLinks();
        System.out.println(setFromOneLink.size());
        if (setFromOneLink.isEmpty()) {

        }
        else {
            List<ParseLinksTask> taskList = new ArrayList<>();
            for (String innerLink : setFromOneLink) {

                linksHashMap.put(innerLink, url);
                //invokeAll(new ParseLinksTask(innerLink));
                ParseLinksTask parseLinksTask = new ParseLinksTask(innerLink);
                taskList.add(parseLinksTask);
            }
            ForkJoinTask.invokeAll(taskList);
        }
    }

    public String cutPath(String path){
        int index = 0;
        for(int i = 0; i<3; i++){
            int begIndex = path.indexOf("/", index+1);
            index = begIndex;
        }
        String newPath = path.substring(index);

        return newPath;
    }




}
