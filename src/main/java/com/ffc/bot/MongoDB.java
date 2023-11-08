package main.java.com.ffc.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import main.java.com.ffc.bot.scheduler.ActionType;
import main.java.com.ffc.bot.scheduler.ScheduledTask;
import main.java.com.ffc.bot.scheduler.WeekNumber;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.state.SwapRequestState;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

public class MongoDB {

    public static final String EMPTY_QUEUE_MEMBER = "_";
    public static final String DEFAULT_SAVED_QUEUE_NAME = "PREVIOUS QUEUE";

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

    private static final String DATABASE_SAVED_QUEUES = "savedQueues";
    public static final String SAVED_QUEUE_ID = "ID";
    public static final String SAVED_QUEUE_NAME = "QUEUE_NAME";
    public static final String SAVED_QUEUE = "QUEUE";

    private static final String DATABASE_SCHEDULED_TASKS = "scheduledTasks";
    public static final String TASK_CHAT_ID = "TASK_CHAT_ID";
    public static final String TASK_NAME = "TASK_NAME";
    public static final String TASK_OBJECT = "TASK_OBJECT";

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

    public static void createNewSavedQueue(String chatId, String savedQueueName, String savedQueue) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SAVED_QUEUES);

        try {
            customersCollection.insertOne(new Document()
                    .append(SAVED_QUEUE_ID, chatId)
                    .append(SAVED_QUEUE_NAME,savedQueueName)
                    .append(SAVED_QUEUE,savedQueue));
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static boolean updateSavedQueue(String chatId, String savedQueueName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SAVED_QUEUES);

        UpdateResult result = customersCollection.updateOne(new Document("$and", Arrays.asList(
                new Document(SAVED_QUEUE_ID, chatId),
                new Document(SAVED_QUEUE_NAME, savedQueueName)
        )), new Document("$set", new Document(SAVED_QUEUE,getQueue(chatId))));

        return result.wasAcknowledged() && result.getModifiedCount() == 1;
    }

    public static void createNewDefaultSavedQueue(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SAVED_QUEUES);

        try {
            customersCollection.insertOne(new Document()
                    .append(SAVED_QUEUE_ID, chatId)
                    .append(SAVED_QUEUE_NAME,DEFAULT_SAVED_QUEUE_NAME)
                    .append(SAVED_QUEUE, getQueue(chatId)));
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static boolean updateDefaultSavedQueue(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SAVED_QUEUES);

        UpdateResult result = customersCollection.updateOne(new Document("$and", Arrays.asList(
                new Document(SAVED_QUEUE_ID, chatId),
                new Document(SAVED_QUEUE_NAME, DEFAULT_SAVED_QUEUE_NAME)
        )), new Document("$set", new Document(SAVED_QUEUE,getQueue(chatId))));

        return result.wasAcknowledged() && result.getModifiedCount() == 1;
    }

    public static boolean removeSavedQueue(String chatId, String savedQueueName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SAVED_QUEUES);

        DeleteResult result = customersCollection.deleteOne(new Document("$and", Arrays.asList(
                new Document(SAVED_QUEUE_ID, chatId),
                new Document(SAVED_QUEUE_NAME, savedQueueName)
        )));

        return result.wasAcknowledged();
    }

    public static boolean savedQueueExists(String chatId, String savedQueueName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SAVED_QUEUES);

        Document result = customersCollection.find(new Document("$and", Arrays.asList(
                new Document(SAVED_QUEUE_ID, chatId),
                new Document(SAVED_QUEUE_NAME, savedQueueName)
        ))).first();

        return result != null;
    }

    public static String getSavedQueue(String chatId, String savedQueueName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SAVED_QUEUES);

        String result = Objects.requireNonNull(customersCollection.find(new Document("$and", Arrays.asList(
                new Document(SAVED_QUEUE_ID, chatId),
                new Document(SAVED_QUEUE_NAME, savedQueueName)
        ))).first()).getString(SAVED_QUEUE);

        return result;
    }

    public static int getSavedQueuesCount(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SAVED_QUEUES);

        int result = (int) customersCollection.countDocuments(eq(SAVED_QUEUE_ID,chatId));

        return result;
    }

    public static ArrayList<String> getSavedQueuesNames(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SAVED_QUEUES);

        ArrayList<String> result = new ArrayList<>();
        for (Document doc : Objects.requireNonNull(customersCollection.find(eq(SAVED_QUEUE_ID, chatId)))) {
            result.add(doc.getString(SAVED_QUEUE_NAME));
        }

        return result;
    }

    public static void createNewScheduledTask(String chatId, String taskName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SCHEDULED_TASKS);
        var time = ((LocalTime.now().getMinute() + 4)/5)*5;
        try {
            var queueSize = Integer.parseInt(getFieldValue(DEFAULT_QUEUE_SIZE,chatId));
            LocalTime taskTime;
            if(time + 5 > 59) {
                taskTime = LocalTime.now().withMinute(5);
                taskTime = taskTime.plusHours(1);
            } else {
                taskTime = LocalTime.now().withMinute(time + 5);
            }

            customersCollection.insertOne(new Document()
                    .append(TASK_CHAT_ID, chatId)
                    .append(TASK_NAME,taskName)
                    .append(TASK_OBJECT, new ObjectMapper()
                            .registerModule(new JavaTimeModule())
                                .writeValueAsString(new ScheduledTask(
                                    chatId,
                                    taskName,
                                    ActionType.CREATE,
                                    LocalDate.now().getDayOfWeek(),
                                    taskTime,
                                    WeekNumber.FIRST,
                                    queueSize > 0 ? queueSize : HttpClient.getChannelMembersCount(chatId)
                    )))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean updateScheduledTask(String chatId, String taskName, ScheduledTask newTask) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SCHEDULED_TASKS);

        UpdateResult result = null;
        try {
            result = customersCollection.updateOne(new Document("$and", Arrays.asList(
                    new Document(TASK_CHAT_ID, chatId),
                    new Document(TASK_NAME, taskName)
            )), new Document("$set", new Document(TASK_OBJECT,
                    new ObjectMapper()
                            .registerModule(new JavaTimeModule())
                            .writeValueAsString(
                                    newTask)
            )));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result.wasAcknowledged() && result.getModifiedCount() == 1;
    }

    public static ScheduledTask getScheduledTask(String chatId, String taskName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SCHEDULED_TASKS);

        ScheduledTask result = null;

        try {
            var resultJSON = customersCollection.find(
                    new Document("$and", Arrays.asList(
                            new Document(TASK_CHAT_ID, chatId),
                            new Document(TASK_NAME, taskName)
                    ))
            ).first();
            if(resultJSON == null) return null;
            var a = resultJSON.getString(TASK_OBJECT);
            result = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(a,ScheduledTask.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static ArrayList<ScheduledTask> getScheduledTasks() {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SCHEDULED_TASKS);

        ArrayList<ScheduledTask> result = new ArrayList<>();
        for (Document doc : Objects.requireNonNull(customersCollection.find())) {
            try {
                ScheduledTask obj = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(doc.getString(TASK_OBJECT), ScheduledTask.class);
                result.add(obj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return result.size() > 0 ? result : null;
    }

    public static ArrayList<ScheduledTask> getScheduledTasks(String chatId) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SCHEDULED_TASKS);

        ArrayList<ScheduledTask> result = new ArrayList<>();
        for (Document doc : Objects.requireNonNull(customersCollection.find(eq(TASK_CHAT_ID,chatId)))) {
            try {
            ScheduledTask obj = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(doc.getString(TASK_OBJECT), ScheduledTask.class);
                result.add(obj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return result.size() > 0 ? result : null;
    }

    public static boolean deleteScheduledTask(String chatId, String taskName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> customersCollection = mongoDatabase.getCollection(DATABASE_SCHEDULED_TASKS);

        DeleteResult result = customersCollection.deleteOne(new Document("$and", Arrays.asList(
                new Document(TASK_CHAT_ID, chatId),
                new Document(TASK_NAME, taskName)
        )));

        return result.wasAcknowledged();
    }
}
