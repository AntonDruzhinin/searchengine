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
import searchengine.repositories.PageRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

//Класс из раздела мноргопоточность
//Рассмотреть варианты внедрения
public class ParseLinksTask extends RecursiveAction {
    private final String url;
    private final ListPages listPages;

    public ParseLinksTask(String url, ListPages listPages) {
        this.url = url;
        this.listPages = listPages;

    }

    public static volatile HashSet<String> allLinks = new HashSet<>();

    @Getter
    private static volatile HashMap<String, String> linksHashMap = new HashMap<>();
    @Getter

    private static  Connection.Response response;

        //TODO: надо сделать так, чтобы при запуске нового таска в потоке лист страниц создавался заного
        // и после передаче в лист для сайта(перед записью в репозиторий)список очищался

    public TreeSet<String> getLinks(){

        TreeSet<String> treeLinks = new TreeSet<>();
        Document document = null;
        Elements elements = null;

        try {
            String userAgent;
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                   // .referrer("http://www.google.com")
                    .timeout(7000)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true);
            Thread.sleep(250);
            response = connection.execute();

            document = connection.get();
            StringBuilder content = new StringBuilder(document.html());


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
            String link = e.attr("href");

            if(
            //link.endsWith("/") &&
                            !allLinks.contains(link) &&
                            link.startsWith(url) &&
                            !link.contains("#"))
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

    private void addPageToRep(String url, StringBuilder content, int httpStatus){
//        Page page = new Page(siteModel);
//        page.setPath(siteModel.getUrl());
//        page.setCode(1);
//        page.setContent("какойта кантент");
//        pageRepository.save(page);



           // page.setContent("some content");
          if(listPages.getPagesList().stream().anyMatch(page -> cutPath(url).equals(page.getPath()))){

            } else
            {

//                if (content == null) {
//                    content = "";
//                }
                Page page = new Page();
                page.setContent(content.toString());
                page.setPath(cutPath(url));
                page.setCode(getHttpStatus());

                listPages.addPage(page);

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
                ParseLinksTask parseLinksTask = new ParseLinksTask(innerLink, listPages);
                taskList.add(parseLinksTask);
            }
            ForkJoinTask.invokeAll(taskList);
        }
    }

    public String cutPath(String path){
        int index = 0;
        for(int i = 0; i<3; i++){
            int begIndexIndex = path.indexOf("/", index+1);
            index = begIndexIndex;
        }
        if(!path.endsWith("/")){
            path+="/";
        }
        if(index < 0){
            index = path.length() - 1;
        }

        String newPath = path.substring(index);


        return newPath;
    }

    public void erasePagesList() {
        linksHashMap = new HashMap<>();
    }
}
