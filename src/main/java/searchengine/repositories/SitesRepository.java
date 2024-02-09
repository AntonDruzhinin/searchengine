package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;
import searchengine.model.Status;

@Repository

public interface SitesRepository extends CrudRepository<SiteModel, Integer> {

}
