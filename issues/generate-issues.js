const fs = require("fs")
const path = require("path")


const dir = __dirname

const templates = fs.readdirSync(dir).filter(
    x=>x.startsWith("issue1")
)

const objects = [
    "Book",
    "Car",
    "Bike",
    "Hotel",
    "Movie"
]

const original = "Restaurant"

for (let i = 0; i < objects.length; i++) {
    const obj = objects[i];
    const num = i + 2

    for(const t of templates) {
        const content = fs.readFileSync(path.join(dir, t)).toString()
        const newContent = content.replace(new RegExp(original, "g"), obj)
            .replace(new RegExp(original.toLowerCase(), "g"), obj.toLowerCase())
        // replace issue1x with issue<num>x
        const newFileName = t.replace("issue1", `issue${num}`)
        fs.writeFileSync(path.join(dir, newFileName), newContent)
    }

}


