import dotenv from "dotenv";
import cors from "cors";
import express from "express";
import helmet from "helmet";
import morgan from "morgan";
import yup from "yup";
import { nanoid } from "nanoid";
import { urls } from "./db.js";
import path from "path";

dotenv.config();

const app = express();

app.use(helmet()); // To remove insecure HTTP headers
app.use(morgan("common")); // For logging
app.use(cors()); // To set request origin policy
app.use(express.json()); // All requests must be JSON

const requestSchema = yup.object().shape({
	id: yup
		.string()
		.trim()
		.matches(/[\w\-]/i), // Alphanumeric case insensitive
	url: yup.string().trim().required(), // URl format
});

app.get("/", (req, res) => {
	res.json({
		message: "ShortUrl ⏩",
	});
});

app.get("/:id", async (req, res, next) => {
	const { id } = req.params;
	try {
		const data = await urls.findOne({ id: id });
		if (data) {
			if (data.url.startsWith("http")) {
				res.redirect(data.url);
			} else {
				res.redirect(`https://${data.url}`);
			}
			urls.update(
				{ id: id },
				{ $push: { clicks: Math.floor(Date.now() / 1000) } }
			);
		} else {
			res.status(404);
			res.sendFile("/views/404.html", {
				root: path.resolve(path.dirname("")),
			});
		}
	} catch (error) {
		next(error);
	}
});

app.post("/url", async (req, res, next) => {
	let { id, url } = req.body;
	try {
		await requestSchema.validate({
			id,
			url,
		});

		let collision = await urls.findOne({ id: id });

		if (!id) {
			id = nanoid(5).toLowerCase();
			while (collision) {
				id = nanoid(5).toLowerCase();
				collision = await urls.findOne({ id: id });
			}
		} else {
			if (collision) {
				throw new Error("Short URL ID already in use 🚫");
			}
		}
		const created = await urls.insert({ id: id, url: url, clicks: [] });
		res.json({
			message: "Short URL successfully created 👌",
			id: created.id,
			url: created.url,
		});
	} catch (error) {
		error.status = 400;
		next(error);
	}
});

app.use((error, req, res, next) => {
	if (error.status) {
		res.status(error.status);
	} else {
		res.status(500);
	}
	const response = { message: error.message };
	if (process.env.NODE_ENV === "dev") response.stack = error.stack;
	res.json(response);
});

const port = process.env.PORT;
app.listen(port, () => {
	console.log(`Listening on port ${port}`);
});
