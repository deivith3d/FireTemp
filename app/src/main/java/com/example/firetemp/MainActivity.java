package com.example.firetemp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView lvProdutos;
    private ArrayAdapter adapter;
    private List<Produto> listaDeProdutos;

//**************FIREBASE ***********************//
    private FirebaseDatabase firebaseDatabase;
    //classe que faz referencia ao banco de dados
    private DatabaseReference reference;
    // classe que aponta para um nó do banco
    private ChildEventListener childEventListener;
    //classe que escuta eventos nos nós filhos
    private Query query;
    //classe que permite fazer uma consulta no banco
    private FirebaseAuth auth;
    //classe responsável pela autenticação
    private FirebaseAuth.AuthStateListener authStateListener;
    //Classe responsável por ficar "ouvindo" as mudanças na autenticação
//**************FIREBASE ***********************//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.floatingActionButtonInluir);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FormularioActivity.class);
                intent.putExtra("acao", "inserir");
                startActivity(intent);
            }
        });
        lvProdutos = findViewById(R.id.listProdutos);
        listaDeProdutos = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,listaDeProdutos);
        lvProdutos.setAdapter(adapter);

        lvProdutos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

              //  String idProduto = listaDeProdutos.get(position).getId();
                Intent intent = new Intent(MainActivity.this, FormularioActivity.class);
                intent.putExtra("acao", "editar");
                Produto prodSelecionado = listaDeProdutos.get(position);
                intent.putExtra("idProduto", prodSelecionado.getId());
                intent.putExtra("nome", prodSelecionado.getNome());
                intent.putExtra("categoria", prodSelecionado.getCategoria());
                startActivity(intent);
            }
        });

        lvProdutos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //System.out.println("Entrou clique longo");
                excluir(position);
                return true;
            }
        });
        System.out.println("Entrou aqui.");
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

    @Override
    protected void onStart() {
        super.onStart();
        listaDeProdutos.clear(); //limpa a lista de produtos
        firebaseDatabase = FirebaseDatabase.getInstance(); //pega uma instancia do banco
        reference = firebaseDatabase.getReference();//volta para o nó raiz do Firebase
        query = reference.child("produtos").orderByChild("nome");//cria consulta baseada nos nomes
        childEventListener = new ChildEventListener() {//fica ouvindo alterações nos nós filhos

                 @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot,
                                                                  @Nullable String previousChildName) {
                    //metodo quando um novo filho é adicionado
                     try {
                         String idUserProd = snapshot.child("idUsuario").getValue(String.class);
                         if(idUserProd.equals(auth.getCurrentUser().getUid())) {
                             //retorna o idUsuraio como string
                             Produto prod = new Produto();
                             prod.setId(snapshot.getKey());
                             prod.setNome(snapshot.child("nome").getValue(String.class));
                             prod.setCategoria(snapshot.child("categoria").getValue(String.class));
                             prod.setIdUsuario(idUserProd);

                             listaDeProdutos.add(prod); //adiciona o produto na lista
                             //        System.out.println(prod.getNome());

                             adapter.notifyDataSetChanged();
                         }
                     }
                     catch (Exception e)
                     {

                     }
                }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot,
                                       @Nullable String previousChildName) {
                //metodo quando um filho é alterado
                for (Produto p: listaDeProdutos) {
                    if ( p.getId().equals(  snapshot.getKey() ) ){
                        p.setNome( snapshot.child("nome").getValue(String.class) );
                        p.setCategoria( snapshot.child("categoria").getValue(String.class) );
                        adapter.notifyDataSetChanged();
                        //notifica os ouvintes que os dados foram alterados
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (Produto p: listaDeProdutos) {
                    if ( p.getId().equals(  snapshot.getKey() ) ){
                        listaDeProdutos.remove( p );
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        query.addChildEventListener(  childEventListener );
        //Adicione um ouvinte para eventos nos nós filhos
    }

    @Override
    protected void onStop() {
        super.onStop();
        query.removeEventListener( childEventListener );//remove o ouvinte na consulta
    }




    private void excluir(int posicao) {
        Produto prodSelecionado = listaDeProdutos.get(posicao);
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("Excluir...");
        alerta.setIcon(android.R.drawable.ic_delete);
        alerta.setMessage("Confirma a exclusão do produto " + prodSelecionado.getNome() + " ?");
        alerta.setNeutralButton("Cancelar", null);
        alerta.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                reference.child("produtos").child( prodSelecionado.getId() ).removeValue();
                //efetivamente remove o produto selecionado

            }
        });
        alerta.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        carregarLista();
    }

    private void carregarLista(){
        if( listaDeProdutos.size() == 0 ){
            Produto fake = new Produto("Lista Vazia ", "");
            listaDeProdutos.add( fake );
            lvProdutos.setEnabled(false);
        }else {
            lvProdutos.setEnabled(true);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //menu.add( "Novo item" );
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuSair) {
            auth.signOut(); // Logoof do user atual
        }
        if( id == R.id.menuAddProduto){
            Intent intent = new Intent( MainActivity.this, FormularioActivity.class);
            intent.putExtra("acao", "inserir");
            startActivity( intent );
        }
        return super.onOptionsItemSelected(item);
    }

}
