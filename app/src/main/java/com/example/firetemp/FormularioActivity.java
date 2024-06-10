package com.example.firetemp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

//import com.example.newbanco.R;

public class FormularioActivity extends AppCompatActivity {

    private EditText etNome;
    private TextView tvID;
    private Spinner spCategorias;
    private Button btSalvar;
    private String acao;
    private Produto produto;

    //**************FIREBASE ***********************//
    private FirebaseDatabase firebaseDatabase;
    //classe que faz referencia ao banco de dados
    private DatabaseReference reference;
    // classe que aponta para um nó do banco
     private FirebaseAuth auth;
    //classe responsável pela autenticação
    private FirebaseAuth.AuthStateListener authStateListener;
    //Classe responsável por ficar "ouvindo" as mudanças na autenticação
//**************FIREBASE ***********************//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        firebaseDatabase = FirebaseDatabase.getInstance();//instancia o Firebase para gravar dados
        reference = firebaseDatabase.getReference();//se possiona no nó raiz


        etNome = findViewById(R.id.editNome);
        spCategorias = findViewById(R.id.spinnerCategoria);
        btSalvar = findViewById(R.id.buttonSalvar);
        btSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvar();
            }
        });

        acao = getIntent().getStringExtra("acao");

        if(acao.equals("editar"))
        {
            carregarFormulario();
        }


        //**************FIREBASE ***********************//
        auth = FirebaseAuth.getInstance();
        //Fica "escutando" as mudanças
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if( auth.getCurrentUser() == null ){
                    finish();
                }
            }
        };
        auth.addAuthStateListener( authStateListener ); //adiciona um ouvinte
        //**************FIREBASE ***********************//
    }

    private void salvar(){
        String nome = etNome.getText().toString();
        if( nome.isEmpty() || spCategorias.getSelectedItemPosition() == 0 ){
            Toast.makeText(this, "Você deve preencher todos os campos!",
                    Toast.LENGTH_LONG).show();
        }else {
            if(  acao.equals("inserir") )
            {
                produto = new Produto();
            }
            produto.setNome( nome );
            produto.setCategoria( spCategorias.getSelectedItem().toString()  );
            produto.setIdUsuario(  auth.getCurrentUser().getUid()  );

            if(  acao.equals("inserir") ) {
                reference.child("produtos").push().setValue( produto );
                //push -> gera um identificador único para id do produto
                etNome.setText("");
                spCategorias.setSelection(0, true);
            }else{
                String idProduto = produto.getId();
                produto.setId( null );
                reference.child("produtos").child( idProduto ).setValue( produto );
                //altera o produto
                finish();
            }
        }
    }



    private void carregarFormulario(){
        String idProduto = getIntent().getStringExtra("idProduto");

        produto = new Produto();
        produto.setId(  idProduto  );
        produto.setNome(  getIntent().getStringExtra("nome") );
        produto.setCategoria( getIntent().getStringExtra("categoria") );
        etNome.setText( produto.getNome() );
        String[] categorias = getResources().getStringArray(R.array.strCategorias);
        for( int i = 0; i < categorias.length ; i++){
            if( produto.getCategoria().equals( categorias[i]  )){
                spCategorias.setSelection( i );
                break;
            }
        }
    }

}