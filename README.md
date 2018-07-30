# AlarmePrecoce

Le principe général du système d’alarme précoce est de déclencher des appels téléphoniques vocaux interactifs et de diffuser des messages d’alertes par différents modes de communication (SMS, e-mail) en fonction d’un certain nombre de déclencheurs (*triggers*).

Un déclencheur peut être une sismicité importante, le dépassement de valeurs de seuil (RSAM1 par exemple), une panne matérielle, logicielle ou électrique, etc.

Les déclencheurs sont créés par des logiciels spécifiques, hébergées sur des serveurs informatiques. Les déclencheurs transitent via le réseau informatique.

Lorsqu’un déclencheur est émis, le système d’alarme précoce l’analyse. Il en tire un certain nombre d’informations :

* L’annuaire à utiliser pour les appels ; 
* La séquence de confirmation ;
* La priorité du message ;
* Le message en lui-même ;
* *etc.*

Commence ensuite une phase d’appel. Celle-ci se base sur la liste d’appel contenant un nombre illimité de numéros de téléphone. Le système d’alarme commence par le premier contact de la liste et initie l’appel.

L’appel émis est vocal et interactif. Le message pré-enregistré est énoncé et la personne appelée doit saisir une séquence de confirmation (suite de numéros à saisir sur le clavier de son téléphone) pour valider la réception du message.

Tant que la séquence de validation n’a pas été enregistrée ou l’appel décroché, l’application va continuer d’appeler les numéros présents dans son annuaire. La phase d’appel laisse sonner un nombre maximum de fois avant d’abandonner un appel. Elle détermine lorsqu’un répondeur prend l’appel et abandonne l’appel. Elle raccroche tout appel décroché au bout d’un certain délai, que l’utilisateur ait ou non entré la séquence de confirmation (pour libérer la ligne et passer à l’appel suivant).

De plus, si la passerelle téléphonique par défaut (Asterisk) est inaccessible, l’application peut également utiliser une alarme anti-intrusion (module Charon I) comme passerelle téléphonique pour émettre une alerte d’urgence.

Enfin, l’application intègre différents outils, notamment une interface Web d’édition d’annuaires, un logiciel d’envoi de triggers permettant la vérification du bon fonctionnement du système.
