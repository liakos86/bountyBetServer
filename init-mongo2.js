print("✅ MongoDB initializing!");

// Switch to the admin database
db = db.getSiblingDB("admin");

// Check if user already exists
if (!db.getUser("liakos86")) {
    db.createUser({
        user: "liakos86",
        pwd: "art78tha3M2",
        roles: [{ role: "root", db: "admin" }]
    });
    print("✅ Admin user 'liakos86' created successfully!");
} else {
    print("⚠️ Admin user 'liakos86' already exists.");
}

// Switch to bountyBetDB
db = db.getSiblingDB("bountyBetDB");

// Ensure the database is created by adding a collection
if (!db.getUser("bountyBetUser")) {
    db.createUser({
        user: "bountyBetUser",
        pwd: "a7fdy4hTXZWeL1kP",
        roles: [{ role: "readWrite", db: "bountyBetDB" }]
    });
    print("✅ Database user 'bountyBetUser' created successfully!");
} else {
    print("⚠️ Database user 'bountyBetUser' already exists.");
}

// Create collection only if it does not exist
if (!db.getCollectionNames().includes("initCollection")) {
    db.createCollection("initCollection");
    db.initCollection.insertOne({ initialized: true });
    print("✅ Collection 'initCollection' created successfully!");
} else {
    print("⚠️ Collection 'initCollection' already exists.");
}
