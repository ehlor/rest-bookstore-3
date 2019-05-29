from flask import Flask, g, request
from flask_restful import Resource, reqparse
import shelve

app = Flask(__name__)

# Valdo uzrasu saraso gyvavima
class NotesList(Resource):
    def put(self):
        return {'message': 'Action prohibited. ', 'data': {}}, 405
    
    def delete(self):
        return {'message': 'Action prohibited. ', 'data': {}}, 405
    
    # GET - grazinimas
    def get(self):
        entries = database()
        elements = list(entries.keys())
        
        notes = [entries[e] for e in elements]
        
        # Parametrizuotas grazinimas - pagal 'author'
        if 'author' in request.args:
            notes = [entries[e] for e in entries if entries[e].author == request.args.get('author')]

        return {'message': 'Notes returned', 'data': notes}, 200
    
    # POST - kurimas
    def post(self):
        parser = reqparse.RequestParser()

        parser.add_argument('title', required=True)
        parser.add_argument('author', required=True)
        parser.add_argument('comment', required=True)
        parser.add_argument('expiration', required=True)

        args = parser.parse_args()

        entries = database()
        
        if args['title'] in entries:
            return {'message': 'A note with this title already exists', 'data': args}, 404
        
        entries[args['title']] = args

        return {'message': 'New note added', 'data': args}, 201

    
# duombazes uzkrovimas
def database():
    if 'db' not in g:
        g.db = shelve.open('notes.db')

    return g.db

# duombazes panaikinimas
@app.teardown_appcontext
def teardown_db():
    db = g.pop('db', None)

    if db is not None:
        db.close()
