// Script pour vérifier la base de données MongoDB
// Utilisation : mongosh < check-db.js

use chat_db

print("\n=== STATISTIQUES ===");
print("Nombre total de messages: " + db.messages.countDocuments());

print("\n=== CONVERSATIONS ===");
var conversations = db.messages.distinct("conversationId");
print("Conversations actives: " + conversations.length);
conversations.forEach(function(conv) {
    var count = db.messages.countDocuments({conversationId: conv});
    print("  - " + conv + ": " + count + " messages");
});

print("\n=== DERNIERS MESSAGES ===");
db.messages.find().sort({timestamp: -1}).limit(5).forEach(function(msg) {
    print("  [" + msg.timestamp + "] " + msg.senderId + " -> " + msg.receiverId + ": " + msg.content);
});

print("\n=== TOUS LES MESSAGES ===");
db.messages.find().sort({timestamp: 1}).forEach(function(msg) {
    printjson(msg);
});
