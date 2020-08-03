import monk from "monk";
import dotenv from "dotenv";
dotenv.config();

const db = monk(process.env.MONGO_URI, {
	password: process.env.MONGO_PASS,
});
db.then(() =>
	console.log(`Connected to Mongo database on ${process.env.MONGO_URI}`)
);
const urls = db.get("urls");
urls.createIndex({ id: 1 }, { unique: true });

export { urls };
