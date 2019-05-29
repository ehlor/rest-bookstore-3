# RESTful Web Service - Book Store

Web serviso paleidimas Docker aplinkoje:
1. Klonuojam git repozitoriją
```
git clone https://github.com/theelo/rest-bookstore-2.git
```
2. Paleidžiam konteinerius
```
docker-compose up
```
Funkcijos:

**GET**

Gauti visas knygas ```/books```

Gauti knygą pagal id ```/books/{id}```

(2 lab.) Gauti visus knygos atsiliepimus ```/books/{id}/reviews```

(2 lab.) Gauti visas knygas su atsiliepimais ```/books?embedded=reviews```

**POST**

Patalpinti knygą ```/books```

**PUT**

Redaguoti knygą ```/books/{id}```

**PATCH**

Redaguoti knygos dalį ```/books/{id}```

(2 lab.) Įkelti knygos atsiliepimą ```/books/{id}/reviews``` 
Pvz.: 
```
{"title" : "Nauja knyga - naujas nusivylimas",
 "author":"Antanas V.",
 "comment":"Bla bla bla bla bla bla bla.",
 "expiration":"2019-04-01"}
 ```

**DELETE**

Ištrinti knygą ```/books/{id}```


**Resurso atributai**: id, name, author, genre.
