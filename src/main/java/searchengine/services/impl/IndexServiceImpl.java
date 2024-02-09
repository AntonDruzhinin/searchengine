package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SiteList;
import searchengine.dto.statistics.*;
import searchengine.model.*;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SitesRepository;
import searchengine.services.IndexService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {
    @Autowired
    private SitesRepository sitesRepository;
    @Autowired
    private PageRepository pageRepository;


    private final SiteList siteList;
    private final ExecutorService service = Executors.newCachedThreadPool();
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();







    public void startIndexing() throws Exception {

        List<searchengine.config.Site> links = siteList.getSites();
        for(Site link : links){
           // checkSite(link);
            addSite(link);
        }
        getPages();
        IndexingResponse response = new IndexingResponse();
        response.setResult(true);
        response.setResponse("Чёт получается" +
                "");

    }

    public void stopIndexing(){
        service.shutdown();
        forkJoinPool.shutdown();
    }

    private void checkSite(Site link){
        Iterable<SiteModel> urls = sitesRepository.findAll();
        for(SiteModel siteModel : urls){
            if(siteModel.getUrl().equals(link.getUrl())){
                sitesRepository.deleteById(siteModel.getId());
                addSite(link);
               // List<Page> pagesToDelete = pageRepository.findAllBySiteModel(siteModel);
                //pageRepository.deleteAll(pagesToDelete);
            }
            addSite(link);

        }
    }

    private void addSite(searchengine.config.Site link){
        SiteModel siteModel = new SiteModel();
        siteModel.setUrl(link.getUrl());
        siteModel.setName(link.getName());
        siteModel.setStatus(Status.INDEXING);
        siteModel.setLastError("");
        siteModel.setStatusTime(LocalDateTime.now());
        sitesRepository.save(siteModel);
    }

    private void getPages() throws Exception {
        Iterable<SiteModel> siteModels = sitesRepository.findAll();
       // ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        for (SiteModel siteModel : siteModels) {
            service.submit(new Thread(() -> {
                List <Page> pagesToDB = new CopyOnWriteArrayList<>();
                try {
                    pagesToDB.addAll(getPagesList(siteModel));
                    siteModel.setPages(pagesToDB);
                    System.out.println(pagesToDB.size() + "  - столько страниц");
                    pageRepository.saveAll(pagesToDB);
                } catch (Exception e) {
                    e.printStackTrace();
                    siteModel.setLastError(e.getMessage());
                }



//            Page page = new Page(siteModel);
//            page.setPath(siteModel.getUrl());
//            page.setCode(1);
//            page.setContent("какойта кантент");
//            pageRepository.save(page);
//            forkJoinPool.invoke(new ParseLinksTask(pageRepository, siteModel.getUrl()));
                System.out.println(siteModel.getUrl());
                siteModel.setStatus(Status.INDEXED);
                sitesRepository.save(siteModel);
            }));




        }
            // TODO: если произошла ошибка выводить статус FAiled и причину ошибки
            // Запускать каждый сайт в отдельном потоке( возможно при вызове метода сделать
            // Runnable класс, который будет создавать конструктор с входящим сайтом и вызывать рекурсивную задачу)
            // если заходил не доьбавлчять сацйт(каждый раз сверять с репозиторием)
    }

    public synchronized List<Page> getPagesList(SiteModel siteModel) throws IOException{

        ListPages listPages = new ListPages(siteModel);
        ParseLinksTask parseLinksTask = new ParseLinksTask(siteModel.getUrl(), listPages);
       // ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        forkJoinPool.invoke(parseLinksTask);
        siteModel.setStatus(Status.INDEXING);
        siteModel.setStatusTime(LocalDateTime.now());

        List<Page> pages = listPages.getPagesList();

       // siteModel.setPages(pages);
//        sitesRepository.save(siteModel);
//        pageRepository.saveAll(pages);
        return pages;
    }

    private static int getPoolSize(){
        try{
            int processorCores = Runtime.getRuntime().availableProcessors();
            return processorCores;
        } catch (Throwable e){
            e.printStackTrace();
            return 2;
        }
    }


    @Override
    public IndexingResponse getIndexingResult() {
        IndexingResponse indexingResponse = new IndexingResponse();
        try {
            startIndexing();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        indexingResponse.setResult(true);
        return indexingResponse;
    }

    @Override
    public IndexingResponse getFalseResult() {
        IndexingResponse indexingResponse = new IndexingResponse();
        indexingResponse.setResult(false);
        indexingResponse.setError("Индексация уже запущена");

        return indexingResponse;
    }

    @Override
    public IndexingResponse getStopIndexingResponse() {
        IndexingResponse response = new IndexingResponse();
        Iterable<SiteModel> siteModelList = sitesRepository.findAll();
        boolean isIndexing = false;
        for (SiteModel siteModel : siteModelList){
            if(siteModel.getStatus().equals(Status.INDEXING)){
               isIndexing = true;
               siteModel.setLastError("Индексация остановлена пользователем");
               siteModel.setStatus(Status.FAILED);
            }
        }
        if(!isIndexing){
            response.setError("Процесс индексации не запущен");
            response.setResult(false);
            return response;}

        try {
            stopIndexing();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        response.setResult(true);
        response.setError(service.isShutdown() + "  еще рабоает");
        System.out.println(Thread.activeCount() + " потоков сейчас работает");
        return response;
    }


}
