# Injection des dépendances

Projet d'apprentissage illustrant les différentes techniques d'**injection des dépendances** en Java, en partant d'une instanciation manuelle jusqu'à l'utilisation du framework **Spring** (versions XML et annotations).

L'objectif est de mettre en œuvre le **couplage faible** entre une couche métier et une couche d'accès aux données (DAO), puis d'injecter ces dépendances de quatre façons différentes.

---

## 🎯 Objectifs (Partie 1)

1. Créer l'interface `IDao` avec une méthode `getData`.
2. Créer une implémentation de cette interface.
3. Créer l'interface `IMetier` avec une méthode `calcul`.
4. Créer une implémentation de cette interface en utilisant le **couplage faible**.
5. Faire l'injection des dépendances :
   - **a.** Par instanciation **statique**
   - **b.** Par instanciation **dynamique**
   - **c.** En utilisant le **Framework Spring**
     - Version **XML**
     - Version **annotations**

---

## 🏗️ Architecture

```
┌─────────────┐        ┌──────────────┐        ┌───────────┐
│   Pres      │ ─────► │    Metier    │ ─────► │    Dao    │
│ (main/test) │        │ (IMetier)    │        │  (IDao)   │
└─────────────┘        └──────────────┘        └───────────┘
```

La couche **Metier** dépend de l'**interface** `IDao` (et non de son implémentation) : c'est le **couplage faible**, qui permet de changer d'implémentation sans modifier le code métier.

### Structure du projet

```
src/main/java/
├── Dao/
│   ├── IDao.java          → Interface DAO (méthode getData)
│   └── DaoImpl.java       → Implémentation DAO (@Component "d")
├── Metier/
│   ├── IMetier.java       → Interface Métier (méthode calculate)
│   └── MetierImpl.java    → Implémentation Métier (@Component "metier", couplage faible)
└── Pres/
    ├── presStatic.java       → (a) Injection statique
    ├── presdynamic.java      → (b) Injection dynamique (réflexion + conf.txt)
    ├── springXml.java        → (c) Injection Spring — version XML
    └── SpringAnnotation.java → (c) Injection Spring — version annotations

src/main/resources/
└── config.xml             → Configuration des beans Spring (version XML)

conf.txt                   → Noms des classes à charger (version dynamique)
pom.xml                    → Dépendances Maven (Spring 6.2.19)
```

---

## 🧩 Les couches

### Couche DAO — `IDao` / `DaoImpl`

```java
public interface IDao {
    double getData();
}
```

`DaoImpl` retourne une valeur (ici `23`) simulant la lecture d'une donnée depuis une source.

### Couche Métier — `IMetier` / `MetierImpl`

```java
public interface IMetier {
    double calculate();
}
```

`MetierImpl` utilise un `IDao` (couplage faible) pour récupérer une donnée et effectuer un calcul (`Math.PI * data`). La dépendance peut être fournie via le **constructeur** ou via le **setter** `setDao(...)`.

---

## 💉 Les 4 techniques d'injection

### a. Injection statique — `presStatic`

L'instanciation se fait **directement dans le code** avec `new`. Le couplage est faible (on passe par les interfaces), mais le choix de l'implémentation est figé à la compilation.

```java
IDao d = new DaoImpl();
MetierImpl metier = new MetierImpl(d);
System.out.println("res=" + metier.calculate());
```

### b. Injection dynamique — `presdynamic`

Les noms des classes sont lus depuis le fichier **`conf.txt`** puis instanciés par **réflexion** (`Class.forName(...)`). On peut changer d'implémentation **sans recompiler**, simplement en modifiant `conf.txt`.

`conf.txt` :
```
Dao.DaoImpl
Metier.MetierImpl
```

```java
Scanner sc = new Scanner(new File("conf.txt"));
Class cDao = Class.forName(sc.nextLine());
IDao d = (IDao) cDao.newInstance();

Class cMetier = Class.forName(sc.nextLine());
IMetier metier = (IMetier) cMetier.getConstructor(IDao.class).newInstance(d);
```

### c.1 Spring — version XML — `springXml`

Les beans et leurs dépendances sont déclarés dans **`config.xml`**. Spring se charge de l'instanciation et de l'injection (via la propriété `dao`).

`config.xml` :
```xml
<bean id="d" class="Dao.DaoImpl"></bean>

<bean id="metier" class="Metier.MetierImpl">
    <property name="dao" ref="d"></property>
</bean>
```

```java
ApplicationContext context = new ClassPathXmlApplicationContext("config.xml");
IMetier metier = context.getBean(IMetier.class);
```

### c.2 Spring — version annotations — `SpringAnnotation`

Les beans sont déclarés directement dans le code via les annotations `@Component` et `@Autowired`. Spring scanne les packages indiqués pour découvrir les composants.

```java
@Component("d")
public class DaoImpl implements IDao { ... }

@Component("metier")
public class MetierImpl implements IMetier {
    @Autowired
    private IDao dao;
    ...
}
```

```java
ApplicationContext context = new AnnotationConfigApplicationContext("Dao", "Metier");
IMetier metier = (IMetier) context.getBean("metier");
```

---

## ⚙️ Prérequis

- **JDK 24**
- **Maven**
- **Spring Context 6.2.19** (récupéré automatiquement par Maven)

---

## ▶️ Exécution

Compiler le projet :

```bash
mvn clean compile
```

Puis exécuter la classe `main` souhaitée selon la technique à tester :

| Technique                 | Classe à exécuter     |
|---------------------------|-----------------------|
| a. Statique               | `Pres.presStatic`     |
| b. Dynamique              | `Pres.presdynamic`    |
| c.1 Spring XML            | `Pres.springXml`      |
| c.2 Spring annotations    | `Pres.SpringAnnotation` |

Exemple avec Maven :

```bash
mvn exec:java -Dexec.mainClass="Pres.presStatic"
```

> 💡 Le plus simple reste de lancer chaque classe `main` directement depuis l'IDE (IntelliJ IDEA).

Résultat attendu (data = 23) :

```
res=72.25...   (Math.PI * 23)
```

---

## 📝 Notes

- La version **dynamique** lit `conf.txt` à la racine du projet : le **répertoire de travail** doit donc être la racine lors de l'exécution.
- Le couplage faible (programmation par interfaces) est la condition indispensable pour pouvoir interchanger librement les implémentations dans les quatre techniques.
