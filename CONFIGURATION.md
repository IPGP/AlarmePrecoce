# Configuration de l'Alarme Précoce

## Configuration de la connexion réseau (network)

La section network ne comprend qu’un paramètre :

* port. Il correspond au port UDP qu’utilise le système d’alarme précoce. Sa valeur par défaut est 4445.

```sh
<network>
  <port>4445</port>
</network>
```

## Configuration des déclencheurs (triggers)

Cette section définit plusieurs paramètres pour les triggers.

Elle comprend une sous-section defaults qui contient les paramètres par défaut des triggers.

* create_trigger_on_error : Indique au système s’il doit générer des appels téléphoniques en cas d’erreurs ou de mauvaise réception de déclencheurs. La valeur de ce paramètre peut être *true* (vrai : on génère des appels en cas d’erreur) ou *false* (faux : on ne génère pas d’appel en cas d’erreur). La valeur par défaut est *false*.
* priority : Indique la priorité par défaut des triggers qui n’ont pas de priorité définit. La valeur de ce paramètre est un entier de 1 à 9. 1 correspond à la priorité la plus élevée. La valeur par défaut est 2.
* confirm_code : Correspond au code de confirmation par défaut des triggers qui n’ont pas de code de confirmation défini.
La valeur de ce paramètre est une suite d’entiers. La valeur par défaut est 11.
* repeat Indique si une liste d’appel doit être répétée en cas de non confirmation. La valeur de ce paramètre peut être *true* (le message sera répété) ou *false* (le message ne sera pas répété). La valeur par défaut est *true*.

```sh
<triggers>
  <create_trigger_on_errors>*false*</create_trigger_on_errors>
  <defaults>
    <priority>2</priority>
    <confirm_code>11</confirm_code>
    <repeat>true</repeat>
  </defaults>
</triggers>
```

## Configuration des passerelles téléphoniques (gateway)

Cette section contient deux paramètres et plusieurs sous-sections, correspondant aux différentes passerelles.

* active : Indique quelle passerelle l’application doit utiliser. Ce paramètre peut prendre les valeurs suivantes : *asterisk* ou *charon*
* failover_enabled : Indique si le failover charon est activé. Ce paramètre peut prendre les valeurs *true* (vrai : failover activé) ou *false* (failover désactivé). La valeur par défaut est *false*

Sous-section asterisk

* retries : Correspond au nombre d’essais auquel a le droit l’opérateur lorsqu’il entre le code
* ring_timeout : Correspond au temps (en millisecondes) pendant lequel le téléphone doit sonner avant de passer à l’opérateur suivant
* agi_server_host : Correspond à l’adresse du serveur AGI local. Dans le cas de l’implémentation par défaut, c’est l’adresse IP de la machine exécutant l’application Alarme Précoce (par défaut : localhost).

Sous-section settings

* ami_host : Le nom d'hôte ou l’adresse IP du serveur Asterisk
* ami_port : Le port qu’utilise l’interface AMI (par défaut 5038)
* ami_user : Le nom d’utilisateur (du manager) à utiliser sur l’AMI
* ami_password : Le mot de passe du manager à utiliser sur l’AMI

Sous section charon

* host : Le nom d'hôte ou l’adresse IP du module Charon
* port : Le port TCP à utiliser pour communiquer avec le module Charon
* timeout : Le timeout à utiliser lors de la communication avec le module.

```sh
<gateway>
  <active>asterisk</active>
  <failover_enabled>true</failover_enabled>
  <asterisk>
    <retries>3</retries>
    <ring_timeout>15000</ring_timeout>
    <agi_server_host>localhost</agi_server_host>
    <settings>
      <ami_host>IP DU SERVEUR ASTERISK</ami_host>
      <ami_port>PORT AMI</ami_port>
      <ami_user>NOM UTILISATEUR AMI</ami_user>
      <ami_password>MOT DE PASSE AMI</ami_password>
    </settings>
  </asterisk>
  <charon>
    <host>195.83.188.220</host>
    <port>23</port>
    <timeout>60000</timeout>
  </charon>
</gateway>
```

## Configuration de la gestion des contacts (contacts)

La section contacts comporte une sous-section et deux paramètres.

* home : Le dossier où se trouvent les objets à servir (pages HTML, scripts JavaScript, etc.).
* port : Le port de connexion HTTP pour l’interface Web

La sous section lists permet de gérer les listes de contacts à utiliser. Elle contient autant de sous-sections qu’il y a des listes de contacts disponibles.
Une liste de contacts se définit de la façon suivante, elle contient deux paramètres.

* id : L’identifiant de la liste de contacts (qui sera utilisé par les triggers);
* path : Le chemin du fichier correspondant à la liste de contacts.

```sh
<contacts>
  <home>resources/www/</home>
  <port>6001</port>
  <lists>
    <list>
      <id>nom_de_la_liste1</id>
      <path>chemin/du/fichier/de/liste1.json</path>
    </list>
    <list>
      <id>nom_de_la_liste2</id>
      <path>chemin/du/fichier/de/liste2.json</path>
    </list>
  <lists>
</contacts>
```

Les identifiants (ici, default et custom_list) sont ensuite utilisable dans les datagrammes des triggers.

* *Remarque* : il doit toujours y avoir une liste default à utiliser lorsque la liste demandée par le trigger n’est pas disponible. Si aucune liste par défaut n’est donnée, EarlyWarning ne démarrera pas.

## Configuration des correspondances entre noms de sons et noms de fichiers

Chaque trigger peut intégrer un son personnalisé (par exemple, un trigger pour un disque plein demandera un son disque_plein et un trigger pour une sismicité importante demandera un son sismicite.)

Cependant, ces noms de sons ne sont que des identifiants, et le son à jouer lors d’un appel téléphonique dépend de l’environnement : dans notre cas, il dépend principalement de la passerelle téléphonique utilisée.

Il faut donc faire un lien entre les identifiants de son (par exemple, sismicite) et les sons correspondants (par exemple, sismicite_importante.gsm).

* id : l'identifiant du son (qui sera utilisé par les triggers)
* asterisk : l'identifiant du son asterisk (cf. plus haut)
* charon : port GPIO du module charon (-1 est utilisé si il n'y a pas de port GPIO prévu pour ce son).

```sh
<sounds>
  <sound>
    <id>default</id>
    <asterisk>warning</asterisk>
    <charon>5</charon>
  </sound>
  <sound>
    <id>welcome</id>
    <asterisk>accueil</asterisk>
    <charon>−1</charon>
  </sound>
  <sound>
    <id>login</id>
    <asterisk>demandecode</asterisk>
    <charon>−1</charon>
  </sound>
  <sound>
</sounds>
```

*Attention* Toutes les passerelles doivent avoir un son d’avertissement par défaut. Il sera joué si un son particulier demandé par un trigger (par exemple, disque_plein) est indisponible.

Un son est dit indisponible qu’à la condition qu’aucun lien ne soit fait entre son identifiant et un fichier dans le fichier de configuration.

*Remarque*, lors du démarrage, l’application EarlyWarning vérifie que toutes les passerelles existantes (c’est- à-dire l’ensemble des passerelles qui ont au moins une clé définie dans sounds) ont un son par défaut. Si ce n’est pas le cas, l’application refuse de démarrer.
De plus, elle émettra un avertissement (Warning) pour tous les sons configurés pour au moins une passerelle, mais pas pour toutes (dans l’exemple, un avertissement aurait été émis pour sismicite pour la passerelle voicent).

## Messagerie électronique (mail)

Cette section définit la fonctionnalité de messagerie électronique (ou e-mail). Elle comprend une sous-section smtp et une sous-section mailinglist. Cette dernière comprend une à plusieurs sous-sections contact.

* use_mail : permet d’activer ou de désactiver la fonctionnalité mail de l’alarme précoce. La valeur peut être *true* (utilisation de la fonctionnalité mail) ou *false* (fonctionnalité mail désactivée)
* host : correspond au serveur d’envoie de mail. La valeur doit être le nom ou l’adresse IP du serveur de mail 
* port : correspond au port réseau du serveur d’envoie de mail. La valeur doit être un entier. Par défaut la valeur 25 est utilisée (port SMTP par défaut)
* username : correspond au nom d’utilisateur du serveur SMTP. La valeur est une chaîne de caractère
* password : correspond au mot de passe de l’utilisateur du serveur SMTP. La valeur est une chaîne de caractère
* from : correspond à l’adresse mail qui envoie le message. La valeur du paramètre est une chaîne de caractère

La sous-section mailinglist peut contenir autant d’éléments contact que l’on veut. Chaque élément contact correspond à un destinataire pour l’envoi de messages. Pour chaque noeud contact, le seul paramètre, email, correspond à l’adresse du destinataire. 

```sh
<mail>
  <use_mail>false</use_mail>
  <smtp>
    <host>smtp.mail.com</host>
    <port>25</port>
    <username>username</username>
    <password>password</password>
    <from>user@mail.com</from>
  </smtp>
  <mailinglist>
    <contact>
      <email>premier.contact@mail.com</email>
    </contact>
    <contact>
      <email>deuxieme.contact@mail.com</email>
    </contact>
  <contact>
</mail>
```
