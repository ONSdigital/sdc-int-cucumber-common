package uk.gov.ons.ctp.common.cloud;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.firestore.FirestoreWait;

/** Access the Cloud data store for testing purposes. */
@Component
public class TestCloudDataStore implements CloudDataStore {
  private static final int DELETION_BATCH_SIZE = 100;

  @Autowired private FirestoreDataStore dataStore;

  private Firestore firestore;

  @PostConstruct
  public void create() {
    firestore = FirestoreOptions.getDefaultInstance().getService();
  }

  public long deleteCollection(String schema) {
    CollectionReference collection = firestore.collection(schema);

    long totalDeleted = 0;
    int numDeletedInBatch = 0;
    do {
      numDeletedInBatch = deleteBatch(collection);
      totalDeleted += numDeletedInBatch;
    } while (numDeletedInBatch >= DELETION_BATCH_SIZE);

    return totalDeleted;
  }

  @Override
  public void deleteObject(String schema, String key) throws CTPException {
    dataStore.deleteObject(schema, key);
  }

  @Override
  public Set<String> getCollectionNames() {
    return dataStore.getCollectionNames();
  }

  @Override
  public <T> Optional<T> retrieveObject(Class<T> target, String schema, String key)
      throws CTPException {
    return dataStore.retrieveObject(target, schema, key);
  }

  @Override
  public <T> List<T> search(Class<T> target, String schema, String[] fieldPath, String searchValue)
      throws CTPException {
    return dataStore.search(target, schema, fieldPath, searchValue);
  }

  @Override
  public void storeObject(String schema, String key, Object value) throws CTPException {
    try {
      dataStore.storeObject(schema, key, value);
    } catch (DataStoreContentionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Wait for an object to appear in the cloud data store.
   *
   * <p>See {@link FirestoreWait} for more details on behaviour.
   *
   * @param collection the collection name
   * @param key the key of the object
   * @param timeoutMillis timeout in milliseconds
   * @return true if the object is found; false if the object is not found within the timeout
   * @throws CTPException on error.
   */
  public boolean waitForObject(String collection, String key, long timeoutMillis)
      throws CTPException {

    if (collection == null || key == null) {
      throw new IllegalArgumentException("collection and key must be provided");
    }

    FirestoreWait firestore =
        FirestoreWait.builder().collection(collection).key(key).timeout(timeoutMillis).build();

    return firestore.waitForObject() != null;
  }

  // there is no firestore method to delete a collection, so we delete in batches.
  private int deleteBatch(CollectionReference collection) {
    int deleted = 0;
    try {
      List<ApiFuture<WriteResult>> deleteFutures = new ArrayList<>();
      ApiFuture<QuerySnapshot> batchCollectionFuture = collection.limit(DELETION_BATCH_SIZE).get();
      List<QueryDocumentSnapshot> documents = batchCollectionFuture.get().getDocuments();
      for (QueryDocumentSnapshot document : documents) {
        deleteFutures.add(document.getReference().delete());
      }
      // wait for all deletes to finish
      for (var f : deleteFutures) {
        f.get();
        ++deleted;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return deleted;
  }
}
