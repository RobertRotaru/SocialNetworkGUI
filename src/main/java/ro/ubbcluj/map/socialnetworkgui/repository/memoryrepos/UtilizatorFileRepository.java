package ro.ubbcluj.map.socialnetworkgui.repository.memoryrepos;

import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.Validator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.StreamSupport;

public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> {

    public UtilizatorFileRepository(String fileName, Validator<Utilizator> validator) {
        super(fileName, validator);
    }

    @Override
    public Utilizator extractEntity(List<String> attributes) throws FileNotFoundException {
        Utilizator user = new Utilizator(attributes.get(1),attributes.get(2));
        user.setId(Long.parseLong(attributes.get(0)));

        return user;
    }

    @Override
    public void reloadWithDeleted(Utilizator utilizator) throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            printWriter.write(""); //delete the content of the file
            if(findAll() != null) {
//                for(var x : findAll()) {
//                    if(!createEntityAsString(x).equals(createEntityAsString(utilizator))) {
//                        //if the user is different from the one we want deleted
//                        writeToFile(x);
//                    }
//                }
                //Equivalent
                StreamSupport.stream(findAll().spliterator(), false)
                        .filter(x->
                                !createEntityAsString(x).equals(createEntityAsString(utilizator))
                                )
                        .forEach(this::writeToFile);
            }
        }catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.toString());
        }
    }

    @Override
    protected String createEntityAsString(Utilizator entity) {
        return entity.getId()+";"+entity.getFirstName()+";"+entity.getLastName();
    }
}
