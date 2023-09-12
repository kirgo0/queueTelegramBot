package main.java.com.ffc.bot;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.state.SwapRequestState;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

public class MongoDB {

    public static final String EMPTY_QUEUE_MEMBER = "_";

    private static MongoClient mongoClient;
    private static final String DATABASE_NAME = "Queue_bot";
    private static final String DATABASE_QUEUES = "queues";

    public static final String QUEUE_ID = "ID";
    public static final String QUEUE = "QUEUE";
    public static final String QUEUE_STATE = "QUEUE_STATE";
    public static final String LAST_MESSAGE_ID = "LAST_MESSAGE_ID";
    public static final String LAST_AUTHORISE_MESSAGE_ID = "LAST_AUTHORISE_MESSAGE_ID";
    public static final String DEFAULT_QUEUE_SIZE = "DEFAULT_QUEUE_SIZE";
    public static final String DEFAULT_QUEUE_VIEW = "DEFAULT_QUEUE_VIEW";

    private static final String DATABASE_USERS = "users";
    public static final String USER_ID = "ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_AUTHORISED = "USER_AUTHORISED";

    private static final String DATABASE_SWAP_REQUESTS = "swapRequests";
    public static final String SWAP_REQUEST_CHAT_ID = "ID";
    public static final String SWAP_FIRST_USER_ID = "FIRST_USER_ID";
    public static final String SWAP_SECOND_USER_ID = "SECOND_USER_ID";
    public static final String SWAP_FIRST_USER_POS = "FIRST_USER_POS";
    public static final String SWAP_SECOND_USER_POS = "SECOND_USER_POS";
    public static final String SWAP_STATE = "REQUEST_STATE";

    private static final String DATABASE_INFO = "info";
    public static final String INFO_ID = "ID";
    public static final String INFO = "INFO";


    public static void connectToDatabase() {
        String uri = PropertiesReader.getProperty("mongo_db");

        mongoClient = MongoClients.create(uri);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);

        try {
            Bson command = new BsonDocument("ping",new BsonInt64(1));
            Document commandResult = mongoDatabase.runCommand(command);
            System.out.println("Connected successfully to server");
        } catch (MongoException e) {
            e.printStackTrace();
        }

    }

    public static void createNewQueue(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_QUEUES);
        try {
            customersCollection.insertOne(new Document()
                    .append(QUEUE_ID, chatId)
                    .append(QUEUE, "")
                    .append(QUEUE_STATE, QueueState.IN_PROCESS.toString())
                    .append(LAST_MESSAGE_ID, "")
                    .append(LAST_AUTHORISE_MESSAGE_ID, "")
                    .append(DEFAULT_QUEUE_SIZE, "0")
                    .append(DEFAULT_QUEUE_VIEW, false));
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static boolean chatRegistered(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_QUEUES);

        Document result = customersCollection.find(eq(QUEUE_ID, chatId)).first();
        return result != null;
    }

    public static boolean queueExists(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_QUEUES);

        Document queueObj = customersCollection.find(eq(QUEUE_ID, chatId)).first();
        boolean result = queueObj != null && (!Objects.equals(queueObj.getString(QUEUE), ""));

        return result;
    }

    public static boolean deleteQueue(String chatId) {
        if(queueExists(chatId)) {
            MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_QUEUES);

            DeleteResult result = customersCollection.deleteOne(eq(QUEUE_ID, chatId));
            return result.wasAcknowledged();
        }
        return false;
    }

    public static boolean updateField(String fieldName, String newValue, String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_QUEUES);

        UpdateResult result = customersCollection.updateOne(eq(QUEUE_ID, chatId), new Document("$set", new Document(fieldName,newValue)));

        return result.wasAcknowledged() && result.getModifiedCount() == 1;
    }

    public static String getFieldValue(String fieldName, String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_QUEUES);

        String result = Objects.requireNonNull(customersCollection.find(eq(QUEUE_ID, chatId)).first()).getString(fieldName);

        return result;
    }

    public static boolean getQueueView(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_QUEUES);

        boolean result = Objects.requireNonNull(customersCollection.find(eq(QUEUE_ID, chatId)).first()).getBoolean(DEFAULT_QUEUE_VIEW);

        return result;
    }

    public static boolean setQueueView(String chatId, boolean value) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_QUEUES);

        UpdateResult result = customersCollection.updateOne(eq(QUEUE_ID, chatId), new Document("$set", new Document(DEFAULT_QUEUE_VIEW, value)));
        return result.wasAcknowledged();
    }

    public static boolean updateQueue(String queue, String chatId) {
        return updateField(QUEUE,queue,chatId);
    }

    public static String getQueue(String chatId) {
        return getFieldValue(QUEUE,chatId);
    }

    public static void createNewUser(String userId, String userName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_USERS);

        try {
            customersCollection.insertOne(new Document()
                    .append(USER_ID, userId)
                    .append(USER_NAME,userName)
                    .append(USER_AUTHORISED,false));
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static boolean authoriseUser(String userId, boolean isAuthorised) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_USERS);

        UpdateResult result = customersCollection.updateOne(eq(USER_ID, userId), new Document("$set", new Document(USER_AUTHORISED,isAuthorised)));

        return result.wasAcknowledged() && result.getModifiedCount() == 1;
    }

    public static boolean userAuthorised(String userId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_USERS);

        Document userDoc = customersCollection.find(eq(USER_ID, userId)).first();

        if(userDoc == null) return false;
        return userDoc.getBoolean(USER_AUTHORISED);
    }

    public static boolean updateUser(String userId, String newUserName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_USERS);

        UpdateResult result = customersCollection.updateOne(eq(USER_ID, userId), new Document("$set", new Document(USER_NAME,newUserName)));

        return result.wasAcknowledged() && result.getModifiedCount() == 1;
    }

    public static boolean userExists(String userId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_USERS);

        Document userDoc = customersCollection.find(eq(USER_ID, userId)).first();

        return userDoc != null;
    }

    public static String getUserName(String userId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_USERS);

        String result = Objects.requireNonNull(customersCollection.find(eq(USER_ID, userId)).first()).getString(USER_NAME);

        return result;
    }

    public static void createNewSwapRequest(String chatId, String firstUserId, String secondUserId, int firstUserPos, int secondUserPos) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SWAP_REQUESTS);
        try {
            customersCollection.insertOne(new Document()
                    .append(SWAP_REQUEST_CHAT_ID, chatId)
                    .append(SWAP_FIRST_USER_ID, firstUserId)
                    .append(SWAP_SECOND_USER_ID, secondUserId)
                    .append(SWAP_FIRST_USER_POS, firstUserPos)
                    .append(SWAP_SECOND_USER_POS, secondUserPos)
                    .append(SWAP_STATE, SwapRequestState.WAITING.toString()));
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static int[] getSwapRequestPositions(String chatId, String firstUserId, String secondUserId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SWAP_REQUESTS);

        int[] result = new int[2];
        result[0] = Objects.requireNonNull(customersCollection.find(new Document("$and", Arrays.asList(
                new Document(SWAP_REQUEST_CHAT_ID, chatId),
                new Document(SWAP_FIRST_USER_ID, firstUserId),
                new Document(SWAP_SECOND_USER_ID, secondUserId)
        ))).first()).getInteger(SWAP_FIRST_USER_POS);
        result[1] = Objects.requireNonNull(customersCollection.find(new Document("$and", Arrays.asList(
                new Document(SWAP_REQUEST_CHAT_ID, chatId),
                new Document(SWAP_FIRST_USER_ID, firstUserId),
                new Document(SWAP_SECOND_USER_ID, secondUserId)
        ))).first()).getInteger(SWAP_SECOND_USER_POS);
        return result;
    }

    public static String getSwapRequestState(String chatId, String firstUserId, String secondUserId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SWAP_REQUESTS);

        String result = Objects.requireNonNull(customersCollection.find(new Document("$and", Arrays.asList(
                new Document(SWAP_REQUEST_CHAT_ID, chatId),
                new Document(SWAP_FIRST_USER_ID, firstUserId),
                new Document(SWAP_SECOND_USER_ID, secondUserId)
        ))).first()).getString(SWAP_STATE);
        return result;
    }

    public static boolean swapRequestExists(String chatId, String firstUserId, String secondUserId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SWAP_REQUESTS);

        Document swapRequestDoc = customersCollection.find(new Document("$and", Arrays.asList(
                new Document(SWAP_REQUEST_CHAT_ID, chatId),
                new Document(SWAP_FIRST_USER_ID, firstUserId),
                new Document(SWAP_SECOND_USER_ID, secondUserId)
        ))).first();

        return swapRequestDoc != null;
    }

    public static boolean deleteSwapRequest(String chatId, String firstUserId, String secondUserId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SWAP_REQUESTS);

        DeleteResult result = customersCollection.deleteOne(new Document("$and", Arrays.asList(
                new Document(SWAP_REQUEST_CHAT_ID, chatId),
                new Document(SWAP_FIRST_USER_ID, firstUserId),
                new Document(SWAP_SECOND_USER_ID, secondUserId)
        )));

        return result.wasAcknowledged();
    }

    public static boolean clearSwapRequests(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SWAP_REQUESTS);

        DeleteResult result = customersCollection.deleteMany(eq(SWAP_REQUEST_CHAT_ID, chatId));

        return result.wasAcknowledged();
    }

    public static void createInfo(int infoId, String info) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_INFO);
        try {
            customersCollection.insertOne(new Document()
                    .append(INFO_ID, infoId)
                    .append(INFO, info));
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static String getInfo(int infoId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_INFO);

        String result = Objects.requireNonNull(customersCollection.find(eq(INFO_ID, infoId)).first()).getString(INFO);

        return result;
    }
}
