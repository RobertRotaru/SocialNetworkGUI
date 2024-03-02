package ro.ubbcluj.map.socialnetworkgui.repository.memoryrepos;

import ro.ubbcluj.map.socialnetworkgui.domain.Prietenie;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.validators.Validator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.StreamSupport;

public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Prietenie>{

    public PrietenieFileRepository(String fileName, Validator<Prietenie> validator) {
        super(fileName, validator);
    }

    @Override
    public Prietenie extractEntity(List<String> attributes) throws FileNotFoundException {
        Prietenie prietenie = new Prietenie();
        prietenie.setId(new Tuple<>(
                Long.parseLong(attributes.get(0)),
                Long.parseLong(attributes.get(1))
        ));

        return prietenie;
    }

    @Override
    public void reloadWithDeleted(Prietenie prietenie) throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(fileName)) {
            printWriter.write(""); //delete the content of the file
            if(findAll() != null) {
//                for(var x : findAll()) {
//                    if(!createEntityAsString(x).equals(createEntityAsString(prietenie))) {
//                        //if the user is different from the one we want deleted
//                        writeToFile(x);
//                    }
//                }
                //Equivalent
                StreamSupport.stream(findAll().spliterator(), false)
                        .filter(x ->
                                !createEntityAsString(x).equals(createEntityAsString(prietenie))
                                )
                        .forEach(this::writeToFile);
            }
        }catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.toString());
        }
    }


    @Override
    protected String createEntityAsString(Prietenie entity) {
        return entity.getId().getLeft().toString() + ";" +
                entity.getId().getRight().toString() + ";" +
                entity.getDate().toString();
    }
}
