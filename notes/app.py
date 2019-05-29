from flask import Flask, render_template, request, g
from flask_restful import Api
import os
import markdown
from noteslist import NotesList
from notes import Notes

app = Flask(__name__)
progInterface = Api(app)

# index vaizdas
@app.route('/', methods=["GET", "POST"])
def home():
    return markdown.markdown(open('README.md', 'r').read())

progInterface.add_resource(NotesList, '/notes')
progInterface.add_resource(Notes, '/notes/<string:title>')

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=True)
