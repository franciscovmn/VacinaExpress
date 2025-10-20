package util;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.config.ObjectClass;
import modelo.Pessoa;
import modelo.Vacinacao;
import modelo.Vacina;

public class Db4oUtil {
    private static final String DATABASE_FILE = "vacina_express.db4o";

    public static ObjectContainer abrirDB() {
        EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();

        // Cascata: atualizar e apagar listas associadas
        ObjectClass pessoaClass = config.common().objectClass(Pessoa.class);
        pessoaClass.objectField("listaVacinacao").cascadeOnUpdate(true);
        pessoaClass.objectField("listaVacinacao").cascadeOnDelete(true);

        ObjectClass vacinacaoClass = config.common().objectClass(Vacinacao.class);
        vacinacaoClass.cascadeOnDelete(true);
        vacinacaoClass.cascadeOnUpdate(true);

        ObjectClass vacinaClass = config.common().objectClass(Vacina.class);
        vacinaClass.objectField("listaVacinacao").cascadeOnUpdate(true);
        vacinaClass.objectField("listaVacinacao").cascadeOnDelete(true);

        return Db4oEmbedded.openFile(config, DATABASE_FILE);
    }

    public static void fecharDB(ObjectContainer db) {
        if (db != null && !db.ext().isClosed()) {
            db.close();
        }
    }
}
