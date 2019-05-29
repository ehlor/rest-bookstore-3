package bookstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class BookAccess{

    public List<Book> getAllBooks(){
        List<Book> bookList = null;
        File file = new File("Books.dat");
        try{
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            bookList = (List<Book>) ois.readObject();
            ois.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        return bookList;
    }

    public void saveBookList(List<Book> bookList){
        try{
            File file = new File("Books.dat");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(bookList);
            oos.close();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public Book getBook(int id){
        List<Book> bookList = getAllBooks();
        for(Book book : bookList){
            if(book.getId() == id) return book;
        }
        return null;
    }

    public int addBook(Book nBook){
        List<Book> bookList = getAllBooks();
        generateId(nBook);
        for(Book book : bookList){
            if(book.equals(nBook) || book.getId().equals(nBook.getId())) return 0;
        }
        bookList.add(nBook);
        saveBookList(bookList);
        return 1;
    }

    public int updateBook(int oId, Book uBook){
        List<Book> bookList = getAllBooks();
        generateId(uBook);
        if(uBook.equals(getBook(oId))){ // jeigu knygos info nepasikeite, iesko ar egzsituoja kita knyga su nauju id
            for(Book book : bookList){
                if(book.getId().equals(uBook.getId()) && (book.getId() != oId)) return 2;
            }
        }
        else if(oId == uBook.getId()){ // jeigu knygos id nepasikeite, iesko ar egzistuoja kita knyga su nauju info
            for(Book book : bookList){
                if(book.equals(uBook)) return 2;
            }
        }
        else{ // iesko ar egzsituoja kita knyga su nauju info ir id
            for(Book book : bookList){
                if(book.equals(uBook) || book.getId().equals(uBook.getId())) return 2;
            }
        }
        for(Book book : bookList){
            if(book.getId() == oId){
                uBook.setReviewList(book.getReviewList());
                uBook.setReviewCount(book.getReviewCount());
                int index = bookList.indexOf(book);
                bookList.set(index, uBook);
                saveBookList(bookList);
                return 1;
            }
        }
        return 0;
    }
    
    public int patchBook(int oId, Book uBook){
        List<Book> bookList = getAllBooks();
        Book nBook = getBook(oId);
        if(uBook.getId() != null) nBook.setId(uBook.getId());
        if(uBook.getName() != null) nBook.setName(uBook.getName());
        if(uBook.getAuthor() != null) nBook.setAuthor(uBook.getAuthor());
        if(uBook.getGenre() != null) nBook.setGenre(uBook.getGenre());
        if(nBook.equals(getBook(oId))){ // jeigu knygos info nepasikeite, iesko ar egzsituoja kita knyga su nauju id
            for(Book book : bookList){
                if(book.getId().equals(nBook.getId()) && (book.getId() != oId)) return 2;
            }
        }
        else if(oId == nBook.getId()){ // jeigu knygos id nepasikeite, iesko ar egzistuoja kita knyga su nauju info
            for(Book book : bookList){
                if(book.equals(nBook)) return 2;
            }
        }
        else{ // iesko ar egzsituoja kita knyga su nauju info ir id
            for(Book book : bookList){
                if(book.equals(nBook) || book.getId().equals(nBook.getId())) return 2;
            }
        }
        for(Book book : bookList){
            if(book.getId() == oId){
                nBook.setReviewList(book.getReviewList());
                nBook.setReviewCount(book.getReviewCount());
                int index = bookList.indexOf(book);
                bookList.set(index, nBook);
                saveBookList(bookList);
                return 1;
            }
        }
        return 0;
    }

    public int deleteBook(int id){
        List<Book> bookList = getAllBooks();
        for(Book book : bookList){
            if(book.getId() == id){
                bookList.remove(book);
                saveBookList(bookList);
                return 1;
            }
        }
        return 0;
    }
        
    public void generateId(Book book){
        if(book.getId() == null){
            int bookId = 1;
            while(true){
                if(getBook(bookId) == null){
                    book.setId(bookId);
                    break;
                }
                else bookId++;
            }
        }
        return;
    }

}
