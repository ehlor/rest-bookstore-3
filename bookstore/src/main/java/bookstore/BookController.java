package bookstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RestController
public class BookController {

    private static final String NOTES = "notes";
    BookAccess bookAccess = new BookAccess();

    @RequestMapping(value = "/books/{id}/reviews", method = RequestMethod.PATCH)
    public ResponseEntity<Response> addReview(@PathVariable(value = "id") int oid, @RequestBody Object review,
                                              UriComponentsBuilder b) throws IOException {
        List<Book> bookList = bookAccess.getAllBooks();
        if(bookAccess.getBook(oid) != null){
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity("http://"+NOTES+":5000/notes", review, String.class);
            if(response.getStatusCodeValue() == 201){
                // get title
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode title = root.path("data").path("title");
                // update book
                for(Book book : bookList){
                    if(book.getId() == oid){
                        book.addToReviewList(title.asText());
                        book.setReviewCount(book.getReviewCount()+1);
                        bookAccess.saveBookList(bookList);
                    }
                }
            }
            else return new ResponseEntity<Response>(new Response("failure", "Bad request or a review with this title already exists"), HttpStatus.BAD_REQUEST);
            HttpHeaders headers = headerBuilder(b, bookAccess.getBook(oid).getId());
            return new ResponseEntity<Response>(new Response("success", "Book review added"), headers, HttpStatus.OK);
        }
        else return new ResponseEntity<Response>(new Response("failure", "Could not find book"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/books/{id}/reviews", method = RequestMethod.GET)
    public ResponseEntity<List<JsonNode>> getBookReviews(@PathVariable(value="id") int id) throws IOException {
        Book book = bookAccess.getBook(id);
        List<JsonNode> reviews = new ArrayList<JsonNode>();
        if(book == null) return new ResponseEntity<List<JsonNode>>(reviews, HttpStatus.NOT_FOUND);
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        ResponseEntity<String> response;
        JsonNode root, data;
        for(String title : book.getReviewList()){
            response = restTemplate.getForEntity("http://"+NOTES+":5000/notes/" + title, String.class);
            root = mapper.readTree(response.getBody());
            data = root.path("data");
            reviews.add(data);
        }
        return new ResponseEntity<List<JsonNode>>(reviews, HttpStatus.OK);
    }

    @RequestMapping(value = "/books", method = RequestMethod.GET)
    public ResponseEntity<List<Book>> getBooks(){
        List<Book> bookList = bookAccess.getAllBooks();
        if(bookList.isEmpty()) return new ResponseEntity<List<Book>>(bookList, HttpStatus.NO_CONTENT);
        else return new ResponseEntity<List<Book>>(bookList, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/books", params = "embedded", method = RequestMethod.GET)
    public ResponseEntity<List<JsonNode>> getBooksEmbedded(@RequestParam("embedded") String embedded) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if(embedded.equals("reviews")){
            List<Book> bookList = bookAccess.getAllBooks();
            JsonNode node;
            List<JsonNode> result = new ArrayList<JsonNode>();
            mapper = new ObjectMapper();
            if(bookList.isEmpty()) return new ResponseEntity<List<JsonNode>>(result, HttpStatus.NO_CONTENT);
            else{
                for(Book book : bookList){
                    try{
                        node = mapper.convertValue(book, JsonNode.class);
                        List<JsonNode> bookReviews = getBookReviews(book.getId()).getBody();
                        ArrayNode array = mapper.valueToTree(bookReviews);
                        ((ObjectNode) node).putArray("reviews").addAll(array);
                        result.add(node);
                    }
                    catch(Exception e){
                        List<Book> booklist = getBooks().getBody();
                        List<JsonNode> res = new ArrayList<JsonNode>();
                        JsonNode n;
                        for(Book bk : booklist){
                            n = mapper.valueToTree(bk);
                            ((ObjectNode) n).put("reviews_error", "Could not fetch data from server.");
                            res.add(n);
                        }
                        return new ResponseEntity<List<JsonNode>>(res, HttpStatus.OK);
                    }
                }
                return new ResponseEntity<List<JsonNode>>(result, HttpStatus.OK);
            }
        }
		else{
            List<Book> booklist = getBooks().getBody();
            List<JsonNode> result = new ArrayList<JsonNode>();
            JsonNode node;
            for(Book book : booklist){
                node = mapper.valueToTree(book);
                result.add(node);
            }
            return new ResponseEntity<List<JsonNode>>(result, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/books/{id}", method = RequestMethod.GET)
    public ResponseEntity<Book> getBook(@PathVariable(value="id") int id){
        Book book = bookAccess.getBook(id);
        if(book == null) return new ResponseEntity<Book>(book, HttpStatus.NOT_FOUND);
        return new ResponseEntity<Book>(book, HttpStatus.OK);
    }

    @RequestMapping(value = "/books", method = RequestMethod.POST)
    public ResponseEntity<Response> addBook(@Valid @RequestBody Book book, 
                                            UriComponentsBuilder b){
        int response = bookAccess.addBook(book);
        // building header
        List<Book> list = bookAccess.getAllBooks();
        Book element = list.get(list.size() - 1);
        HttpHeaders headers = headerBuilder(b, element.getId());

        if(response == 1) return new ResponseEntity<Response>(new Response("success", "Book added"), headers, HttpStatus.CREATED);
        return new ResponseEntity<Response>(new Response("failure", "Book already exists"), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/books/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Response> updateBook(@PathVariable(value="id") int oid,
                                               @Valid @RequestBody Book book,
                                               UriComponentsBuilder b){
        if(bookAccess.getBook(oid) != null){
            int response = bookAccess.updateBook(oid, book);
            Book element;
            HttpHeaders headers;
            if(book.getId() == null){
                element = bookAccess.getBook(oid);
            }
            else{
                element = bookAccess.getBook(book.getId());
            }
            headers = headerBuilder(b, element.getId());

            if(response == 1) return new ResponseEntity<Response>(new Response("success", "Book updated"), headers, HttpStatus.OK);
            else return new ResponseEntity<Response>(new Response("failure", "Book already exists"), HttpStatus.BAD_REQUEST);
        }
        else return new ResponseEntity<Response>(new Response("failure", "Could not find book"), HttpStatus.NOT_FOUND);
    }
    
    @RequestMapping(value = "/books/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<Response> patchBook(@PathVariable(value="id") int oid,
                                              @RequestBody Book book,
                                              UriComponentsBuilder b){
        if(bookAccess.getBook(oid) != null){
            int response = bookAccess.patchBook(oid, book);
            Book element;
            HttpHeaders headers;
            if(book.getId() == null){
                element = bookAccess.getBook(oid);
            }
            else{
                element = bookAccess.getBook(book.getId());
            }
            headers = headerBuilder(b, element.getId());
        
            if(response == 1) return new ResponseEntity<Response>(new Response("success", "Book updated"), headers, HttpStatus.OK);
            else return new ResponseEntity<Response>(new Response("failure", "Book already exists"), HttpStatus.BAD_REQUEST);
        }
        else return new ResponseEntity<Response>(new Response("failure", "Could not find book"), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/books/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Response> deleteBook(@PathVariable(value="id") int id){
        int response = bookAccess.deleteBook(id);
        if(response == 1) return new ResponseEntity<Response>(new Response("success", "Book deleted"), HttpStatus.OK);
        return new ResponseEntity<Response>(new Response("failure", "Could not find book"), HttpStatus.NOT_FOUND);
    }

    public HttpHeaders headerBuilder(UriComponentsBuilder b, int id){
        UriComponents uriComponents = b.path("/books/{id}").buildAndExpand(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return headers;
    }
}
