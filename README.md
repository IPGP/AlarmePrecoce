# AlarmePrecoce

## Fontionnement général

Le principe général du système d’alarme précoce est de déclencher des appels téléphoniques vocaux interactifs et de diffuser des messages d’alertes par différents modes de communication (SMS, e-mail) en fonction d’un certain nombre de déclencheurs (*triggers*).

Un déclencheur peut être une sismicité importante, le dépassement de valeurs de seuil (moyenne du signal sismique par exemple), une panne matérielle, logicielle ou électrique, etc.

Les déclencheurs sont créés par des logiciels spécifiques, hébergées sur des serveurs informatiques. Les déclencheurs transitent via le réseau informatique.

[Fonctionnement général](https://raw.githubusercontent.com/IPGP/AlarmePrecoce/master/doc/Fonctionnement_General.png "Fonctionnement général")


Lorsqu’un déclencheur est émis, le système d’alarme précoce l’analyse. Il en tire un certain nombre d’informations :

* L’annuaire à utiliser pour les appels ; 
* La séquence de confirmation ;
* La priorité du message ;
* Le message en lui-même ;
* *etc.*

Commence ensuite une phase d’appel. Celle-ci se base sur la liste d’appel contenant un nombre illimité de numéros de téléphone. Le système d’alarme commence par le premier contact de la liste et initie l’appel.

L’appel émis est vocal et interactif. Le message pré-enregistré est énoncé et la personne appelée doit saisir une séquence de confirmation (suite de numéros à saisir sur le clavier de son téléphone) pour valider la réception du message.

Tant que la séquence de validation n’a pas été enregistrée ou l’appel décroché, l’application va continuer d’appeler les numéros présents dans son annuaire. La phase d’appel laisse sonner un nombre maximum de fois avant d’abandonner un appel. Elle détermine lorsqu’un répondeur prend l’appel et abandonne l’appel. Elle raccroche tout appel décroché au bout d’un certain délai, que l’utilisateur ait ou non entré la séquence de confirmation (pour libérer la ligne et passer à l’appel suivant).

De plus, si la passerelle téléphonique par défaut (Asterisk) est inaccessible, l’application peut également utiliser une alarme anti-intrusion (via un module Charon I) comme passerelle téléphonique pour émettre une alerte d’urgence.

Enfin, l’application intègre différents outils, notamment une interface Web d’édition d’annuaires, un logiciel d’envoi de triggers permettant la vérification du bon fonctionnement du système.

## Installation

### Installations des pré-requis

Pour fonctionner, l'application a besoin de :
* L'environnement d'exécution Java (https://www.java.com).
* L'autocommutateur téléphonique Asterisk (https://www.asterisk.org/).

Par exemple sous Debian
```sh
apt-get install asterisk default-jre
```

Voir ici pour la [compilation du projet](COMPILATION.md)

### Configuration d’Asterisk

Cette section couvre très brièvement la configuration d'Asterisk. Pour plus de détails, se référer à la documentation officielle.
Il n'est pas nécessaire de définir un dialplan dans la mesure ou la gestion des appels se fait via l'Asterisk Manager Interface.

#### Configuration de l’Asterisk Manager Interface (AMI)

Editer le fichier /etc/asterisk/manager.conf :

```sh
[general]
enabled = yes
port = <PORT AMI> (par défaut 5038)
bindaddr = 127.0.0.1
[<NOM D’UTILISATEUR AMI>]
enabled = yes
port = 5038
allow = 0.0.0.0/0.0.0.0
bindaddr = <IP DU SERVEUR ASTERISK ICI>
secret = <MOT DE PASSE AMI>
read = all
write = all
```

#### Exemple d'utilisation d'une passerelle SIP/RTC

Pour la configuration d'une connexion Asterisk <=> passerelle SIP/RTC AudioGuides MP-114, éditer le fichier /etc/asterisk/sip.conf et remplacer son contenu par :

```sh
[general]
context=default
disallow=all
allow=ulaw
allow=alaw

[pstn-out]
type=peer
allow=ulaw
context=outbound
dtmfmode=inband
host=<IP DU MP-114> ; IP de la passerelle
nat=no
qualify=no

[pstn-in]
canreinvite=no
context=inbound
dtmfmode=inband
host=<IP DU MP-114> ; IP de la passerelle
nat=never
type=user
```

Redémarrer Asterisk.

#### Ajout des fichiers sons

Asterisk permet d’ajouter des sons correspondant à des messages personnalisés. Ces fichiers doivent être au format "gsm" et copiés dans le dossier /usr/share/asterisk/sounds

*Attention*, après l'ajout de sons, un redémarrage d’Asterisk est nécessaire afin de les réindexer.

Il est possible de convertir plusieurs fichiers "wav" en "gsm" grâce à l'utilitaire sox :

```sh
for i in ∗.wav ; do
	sox "$i" −r 8000 −c1 "$(basename "${i /.wav}").gsm"
done
```

## Configuration du logiciel Alarme Précoce

La configuration de l'application se fait avec le fichier earlywarning.xml. Ce dernier comprend un certain nombre de sections principales [détaillées ici](CONFIGURATION.md).

L'installation se fait avec la commande :

```sh
make install
```

Il est possible de l'installer manuellement en créant un dossier contenant le JAR et le dossier resources. Il faut aussi copier le script de lancement dans le dossier de base, au même niveau que le fichier Jar.
Le dossier contiendra :

```sh
|-- EarlyWarning.jar
|-- EarlyWarning.sh
`-- resources
    |-- EarlyWarning.sh
    |-- contacts
    |-- earlywarning.service
    |-- earlywarning.xml
    |-- log4j.properties
    `-- www
```


## Lancement de l'applcation

Toute modification de la configuration d’Asterisk ou de la configuration d’EarlyWarning demande un redémarrage des applications concernées.

* Redémarrer Asterisk sous Debian :

```sh
service asterisk restart
```

* Alarme Précoce via le script EarlyWarning.sh :

```sh
sh EarlyWarning.sh
```

## Configuration du démarrage automatique via systemd

Une fois l’application installée dans un dossier (à savoir l’archive jar EarlyWarning.jar, le dossier resources ainsi que le fichier de configuration dûment rempli), on peut l’exécuter comme un service (daemon) grâce à systemd (sous Debian).

Celui-ci peut être trouvé dans le dossier resources et doit être configuré puis copié dans /etc/systemd/system/ afin qu’il soit automatiquement exécuté au démarrage.

Pour configurer le fichier, vous aurez besoin

* Du nom d’utilisateur qui exécutera l’application (par défaut sysop)
* De l’emplacement d’installation du script de démarrage de l’application

```sh
[Unit]
Description=Alarme Precoce 
After=syslog.target
After=network.target

[Service]
Type=simple
User=sysop
Group=sysop
WorkingDirectory=/home/sysop/Alarme/
ExecStart=/bin/bash /home/sysop/Alarme/EarlyWarning.sh
Restart=on-failure
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=/var/log/earlywarning.log
Environment=USER=sysop HOME=/home/sysop
#StandardOutput=tty

[Install]
WantedBy=multi-user.target
```

Activer ensuite le service :

```sh
systemctl daemon-reload
systemctl enable earlywarning.service
```

Le service sera démarré automatiquement au prochain démarrage. Pour démarrer manuellement le service, on peut utiliser :

```sh
systemctl start earlywarning.service
```

De même avec stop, restart ou status.

Pour accéder aux journaux de l’application, utiliser la commande :

```sh
journalctl -u earlywarning.service
```

## Modification des listes d’appel via l’interface Web

Les listes d’appel ne sont pas censées être modifiées directement via les fichiers correspondants (pour des risques évidents de risque d’erreur de syntaxe, par exemple).

La modification doit se faire à travers l’interface Web. Pour ce faire :

* Lancer l’application EarlyWarning ;
* Se rendre avec un navigateur Web à l’adresse http://<adresse du serveur EarlyWarning>:<port> (le port par défaut est 6001).

[Interface](https://github.com/IPGP/AlarmePrecoce/doc/Interface_Appels.png "Interface")

### Introduction
L’interface Web se décompose en deux colonnes.

La colonne de gauche correspond à la liste d’appel, à savoir quelles sont les personnes à appeler et dans quel ordre. Les éléments en couleur plus foncée correspondent au contact prioritaire. Ainsi, le premier élément est toujours de couleur foncée.

La colonne de droite se décompose en deux parties :

* L’annuaire contient toutes les personnes connues du fichier ; on peut les y supprimer ou y définir le contact prioritaire ;
* Le formulaire d’ajout de contact permet d’ajouter un nouveau contact au fichier.

Pour ajouter un contact à la liste d’appel, il suffit de faire glisser le contact correspondant de la liste de droite à celle de gauche, à la position souhaitée.

Pour modifier l’ordre d’appel, on peut directement réordonner (en les faisant glisser) les éléments de la liste de gauche.

### Ajout d’un contact

L’ajout d’un contact à la liste d’appels se fait de la façon suivante : — Remplir les champs Nom et Téléphone ;

* Cocher au besoin la case Prioritaire ;
* Valider avec le bouton Valider.

### Case Prioritaire

La case Prioritaire permet de définir un contact comme étant prioritaire. Cela signifie que le premier de la liste d’appel sera toujours celui-ci.

Un contact prioritaire peut se trouver à plusieurs endroits dans la liste d’appels, mais le premier élément sera toujours un contact prioritaire (si la liste d’appel contient un contact prioritaire).

Un seul contact peut être prioritaire à la fois.

### Désactivation d’un contact

Pour désactiver un contact (ou, s’il est présent plusieurs fois dans la liste d’appel, supprimer une de ses occurrences), utiliser la croix qui apparaît à gauche de l’élément lorsque l’on passe le pointeur de la souris sur l’élément dans la liste d’appel (à droite).

Vous ne pouvez pas supprimer tous les contacts de la liste d’appel.

### Suppression d’un contact

Pour supprimer un contact de la liste d’appel, utiliser la croix qui apparaît à gauche de l’élément correspon- dant dans la liste des contacts disponibles (à gauche).

Supprimer un contact supprime également toutes ses occurrences dans la liste d’appel.

### Réglage du contact prioritaire

Pour définir le contact prioritaire, utiliser le bouton avec les deux flèches vers le haut qui apparaît au passage de la souris dans l’annuaire.

Une nouvelle entrée sera alors automatiquement ajoutée à la liste d’appel pour que le nouveau contact prioritaire soit présent en haut.

## Contribution

N'hésitez pas à nous contacter si vous souhaitez participer au développement de ce projet.

## Auteurs

* **Patrice Boissier** - *Software engineer - OVPF/IPGP* - [PBoissier](https://github.com/PBoissier) - boissier@ipgp.fr
* **Thomas Kowalski** - *Etudiant en informatique* - [](https://github.com/KowalskiThomas) - thom.kowa@gmail.com

## License

Ce projet est sous licence GNU General Pyblic License v3.0 - voir le fichier [License](LICENSE) pour plus de détails.
