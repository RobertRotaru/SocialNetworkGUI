package ro.ubbcluj.map.socialnetworkgui.repository.memoryrepos;


import ro.ubbcluj.map.socialnetworkgui.domain.Entity;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.Validator;
import ro.ubbcluj.map.socialnetworkgui.repository.InMemoryRepository;

import java.io.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID,E> {
    String fileName;

    public AbstractFileRepository(String fileName, Validator<E> validator) {
        super(validator);
        this.fileName = fileName;
        loadData();

    }

    private void loadData() { //decorator pattern
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String newLine;
            while ((newLine = reader.readLine()) != null) {
                List<String> data = Arrays.asList(newLine.split(";"));
                E entity = extractEntity(data);
                super.save(entity);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Path path= Paths.get(fileName);
//        try{
//            List<String> lines= Files.readAllLines(path);
//            lines.forEach(line -> {
//                E entity=extractEntity(Arrays.asList(line.split(";")));
//                super.save(entity);
//            });
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }

    }

    /**
     * extract entity  - template method design pattern
     * creates an entity of type E having a specified list of @code attributes
     *
     * @param attributes
     * @return an entity of type E
     */
    public abstract E extractEntity(List<String> attributes) throws FileNotFoundException;  //Template Method

    public abstract void reloadWithDeleted(E e) throws FileNotFoundException;

    protected abstract String createEntityAsString(E entity); //Template Method

    @Override
    public Optional<E> save(E entity) {
        var result = super.save(entity);
        if (result.isEmpty())
            writeToFile(entity);
        return result;

    }

    @Override
    public Optional<E> delete(ID id) throws FileNotFoundException {
        Optional<E> result = super.delete(id);
        if(result.isPresent()) {
            reloadWithDeleted(result.get());
        }
        return result;
    }

    protected void writeToFile(E entity) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {

            writer.write(createEntityAsString(entity));
            writer.newLine();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
