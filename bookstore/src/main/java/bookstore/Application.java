package bookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class Application{

    private static final String NOTES = "notes";

    @PostConstruct
    private void init(){
        List<Book> bookList = null;
        
        RestTemplate restTemplate = new RestTemplate();
        try{
            File file = new File("Books.dat");
            String jsonString1 = "{\"title\":\"Nauja knyga - naujas nusivylimas\",\"author\":\"Antanas V.\",\"comment\":\"Bla bla bla bla bla bla bla.\",\"expiration\":\"2019-04-01\"}";
            String jsonString2 = "{\"title\":\"Nauja knyga\",\"author\":\"Antanas V.\",\"comment\":\"Bla bla bla bla bla bla bla.\",\"expiration\":\"2019-04-01\"}";
            String jsonString3 = "{\"title\":\"Knyga\",\"author\":\"Antanas V.\",\"comment\":\"Bla bla bla bla bla bla bla.\",\"expiration\":\"2019-04-01\"}";
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj1 = mapper.readTree(jsonString1);
            JsonNode actualObj2 = mapper.readTree(jsonString2);
            JsonNode actualObj3 = mapper.readTree(jsonString3);
            if(!file.exists()){
                Book book1 = new Book(12340, "George Orwell", "1984", "Post-Apocalyptic Fiction");
                Book book2 = new Book(8000, "J. R. R. Tolkien", "The Lord of the Rings", "Fantasy");
                Book book3 = new Book(9000, "Harper Lee", "To Kill a Mockingbird", "Southern Gothic Fiction");
                Book book4 = new Book(555, "Jane Austen", "Pride and Prejudice", "Comedy");
                try{
                    restTemplate.postForEntity("http://"+NOTES+":5000/notes", actualObj1, String.class);
                    restTemplate.postForEntity("http://"+NOTES+":5000/notes", actualObj2, String.class);
                    restTemplate.postForEntity("http://"+NOTES+":5000/notes", actualObj3, String.class);
                } 
                catch(Exception e){}
                book2.addToReviewList("Nauja knyga - naujas nusivylimas");
                book2.addToReviewList("Nauja knyga");
                book2.setReviewCount(book2.getReviewCount()+2);
                book3.addToReviewList("Knyga");
                book3.setReviewCount(book3.getReviewCount()+1);
                
                bookList = new ArrayList<Book>();
                bookList.add(book1);
                bookList.add(book2);
                bookList.add(book3);
                bookList.add(book4);
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(bookList);
                oos.close();
            }
            else{
                try{
                    restTemplate.postForEntity("http://"+NOTES+":5000/notes", actualObj1, String.class);
                    restTemplate.postForEntity("http://"+NOTES+":5000/notes", actualObj2, String.class);
                    restTemplate.postForEntity("http://"+NOTES+":5000/notes", actualObj3, String.class);
                } 
                catch(Exception e){}
            }
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
