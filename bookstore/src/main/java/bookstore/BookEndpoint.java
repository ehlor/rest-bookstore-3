package bookstore;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.example.bookstore.*;

@Endpoint
public class BookEndpoint {

    private static final String NOTES = "notes";

    BookAccess bookAccess = new BookAccess();
    private static final String NAMESPACE_URI = "http://www.example.org/bookstore";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBooksRequest")
    @ResponsePayload
    public GetBooksResponse getBooks(@RequestPayload GetBooksRequest request) {
        GetBooksResponse response = new GetBooksResponse();
        List<Book> bookList = bookAccess.getAllBooks();
        if(request.getEmbedded() != null && request.getEmbedded().equalsIgnoreCase("reviews")){
            if(bookList.isEmpty()) return response;
            else{
                for(Book book : bookList){
                    BookFullType bt = new BookFullType();
                    bt.setId(book.getId());
                    bt.setName(book.getName());
                    bt.setAuthor(book.getAuthor());
                    bt.setGenre(book.getGenre());
                    bt.setReviewCount(book.getReviewCount());
                    try{
                        GetBookReviewsRequest req = new GetBookReviewsRequest();
                        req.setId(BigInteger.valueOf(book.getId()));
                        GetBookReviewsResponse reviews = getBookReviews(req);
                        for(ReviewType rt : reviews.getReviews()) bt.getReviews().add(rt);
                    }
                    catch(Exception e){
                        bt.setReviewsError("Could not fetch data from server");
                    }
                    response.getBookEmbedded().add(bt);
                }
                return response;
            }
        }
        else{
            if(bookList.isEmpty()) return response;
            else{
                for(Book book : bookList){
                    BookWithRCType bt = new BookWithRCType();
                    bt.setId(book.getId());
                    bt.setName(book.getName());
                    bt.setAuthor(book.getAuthor());
                    bt.setGenre(book.getGenre());
                    bt.setReviewCount(book.getReviewCount());
                    response.getBook().add(bt);
                }
                return response;
            }
        }
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBookRequest")
    @ResponsePayload
    public GetBookResponse getBook(@RequestPayload GetBookRequest request) {
        GetBookResponse response = new GetBookResponse();
        BookWithRCType bt = new BookWithRCType();
        Book book = bookAccess.getBook(request.getId().intValue());
        if(book == null) return response;
        bt.setId(book.getId());
        bt.setName(book.getName());
        bt.setAuthor(book.getAuthor());
        bt.setGenre(book.getGenre());
        bt.setReviewCount(book.getReviewCount());
        response.setBook(bt);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addBookRequest")
    @ResponsePayload
    public AddBookResponse addBook(@RequestPayload AddBookRequest request) {
        AddBookResponse response = new AddBookResponse();
        BookType obook = request.getBook();
        Book book = new Book(obook.getId(), obook.getAuthor(), obook.getName(), obook.getGenre());
        if (bookAccess.addBook(book) == 1)
            response.setResponse("Success. Book added");
        else
            response.setResponse("Failure. Book already exists");
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateBookRequest")
    @ResponsePayload
    public UpdateBookResponse updateBook(@RequestPayload UpdateBookRequest request) {
        UpdateBookResponse response = new UpdateBookResponse();
        int oid = request.getId().intValue();
        BookType obook = request.getBook();
        Book book = new Book(obook.getId(), obook.getAuthor(), obook.getName(), obook.getGenre());
        if (bookAccess.getBook(request.getId().intValue()) != null) {
            int resp = bookAccess.updateBook(oid, book);
            if (resp == 1)
                response.setResponse("Success. Book updated");
            else
                response.setResponse("Failure. Book already exists");
            return response;
        } else
            response.setResponse("Failure. Could not find book");
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "patchBookRequest")
    @ResponsePayload
    public PatchBookResponse patchBook(@RequestPayload PatchBookRequest request) {
        PatchBookResponse response = new PatchBookResponse();
        int oid = request.getId().intValue();
        BookOptionalType obook = request.getBook();
        Book book = new Book(obook.getId(), obook.getAuthor(), obook.getName(), obook.getGenre());
        if (bookAccess.getBook(request.getId().intValue()) != null) {
            int resp = bookAccess.patchBook(oid, book);
            if (resp == 1)
                response.setResponse("Success. Book updated");
            else
                response.setResponse("Failure. Book already exists");
            return response;
        } else
            response.setResponse("Failure. Could not find book");
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteBookRequest")
    @ResponsePayload
    public DeleteBookResponse deleteBook(@RequestPayload DeleteBookRequest request) {
        DeleteBookResponse response = new DeleteBookResponse();
        if (bookAccess.deleteBook(request.getId().intValue()) == 1)
            response.setResponse("Success. Book deleted");
        else
            response.setResponse("Failure. Book does not exists");
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addReviewRequest")
    @ResponsePayload
    public AddReviewResponse addReview(@RequestPayload AddReviewRequest request) throws IOException {
        AddReviewResponse response = new AddReviewResponse();
        int oid = request.getId().intValue();
        List<Book> bookList = bookAccess.getAllBooks();
        if (bookAccess.getBook(oid) != null) {
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jNode = mapper.createObjectNode();
            jNode.put("title", request.getReview().getTitle());
            jNode.put("author", request.getReview().getAuthor());
            jNode.put("comment", request.getReview().getComment());
            jNode.put("expiration", request.getReview().getDate());
            try{
                ResponseEntity<String> resp = restTemplate.postForEntity("http://"+NOTES+":5000/notes", jNode,
                        String.class);
                if (resp.getStatusCodeValue() == 201) {
                    // get title
                    JsonNode root = mapper.readTree(resp.getBody());
                    JsonNode title = root.path("data").path("title");
                    // update book
                    for (Book book : bookList) {
                        if (book.getId() == oid) {
                            book.addToReviewList(title.asText());
                            book.setReviewCount(book.getReviewCount() + 1);
                            bookAccess.saveBookList(bookList);
                            break;
                        }
                    }
                    response.setResponse("Success. Book review added");
                } else
                    response.setResponse("Failure. Bad request or a review with this title already exist");
            }
            catch(Exception e){
                restTemplate.put("http://"+NOTES+":5000/notes/"+request.getReview().getTitle(), jNode, String.class);
                response.setResponse("Success. Book updated");
            }
        } else
            response.setResponse("Failure. Could not find book");
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBookReviewsRequest")
    @ResponsePayload
    public GetBookReviewsResponse getBookReviews(@RequestPayload GetBookReviewsRequest request)
            throws IOException, DatatypeConfigurationException {
        GetBookReviewsResponse response = new GetBookReviewsResponse();
        int id = request.getId().intValue();
        Book book = bookAccess.getBook(id);
        
        if(book == null) return response;
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        ResponseEntity<String> resp;
        JsonNode root, data;
        for(String title : book.getReviewList()){
            ReviewType review = new ReviewType();
            resp = restTemplate.getForEntity("http://"+NOTES+":5000/notes/" + title, String.class);
            root = mapper.readTree(resp.getBody());
            data = root.path("data");
            review.setTitle(data.get("title").asText());
            review.setAuthor(data.get("author").asText());
            review.setComment(data.get("comment").asText());
            review.setDate(data.get("expiration").asText());
            response.getReviews().add(review);
        }
        return response;
    }
}