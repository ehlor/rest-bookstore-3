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

**WSDL failas:** /soap/books/bookstore.wsdl
SOAP užklausas siųsti į /soap/books

Užklausų pvz.:
**Gauti visas knygas su embedded komentarais**
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:book="http://www.example.org/bookstore">
   <soapenv:Header/>
   <soapenv:Body>
      <book:getBooksRequest>
         <book:embedded>reviews</book:embedded>
      </book:getBooksRequest>
   </soapenv:Body>
</soapenv:Envelope>
```
**Pridėti komentarą į 12340 knygą**
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:book="http://www.example.org/bookstore">
   <soapenv:Header/>
   <soapenv:Body>
      <book:addReviewRequest>
         <book:id>12340</book:id>
         <book:review>
            <book:title>Pavadinimas</book:title>
            <book:author>Autorius</book:author>
            <book:comment>Komentaras</book:comment>
            <book:date>2015</book:date>
         </book:review>
      </book:addReviewRequest>
   </soapenv:Body>
</soapenv:Envelope>
```
**Pakeisti 9000 knygos pavadinimą ir žanrą**
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:book="http://www.example.org/bookstore">
   <soapenv:Header/>
   <soapenv:Body>
      <book:patchBookRequest>
         <book:id>9000</book:id>
         <book:book>
            <book:name>Naujas pavadinimas</book:name>
            <book:genre>Naujas zanras</book:genre>
         </book:book>
      </book:patchBookRequest>
   </soapenv:Body>
</soapenv:Envelope>
```
***Gauti 9000 knygą***
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:book="http://www.example.org/bookstore">
   <soapenv:Header/>
   <soapenv:Body>
      <book:getBookRequest>
         <book:id>9000</book:id>
      </book:getBookRequest>
   </soapenv:Body>
</soapenv:Envelope>
```
