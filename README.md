# 📋 次世代タスク管理システム (フルクラウド・マルチフロントエンド実証プロジェクト)

本プロジェクトは、フリーランスの即戦力PL/テクニカルリードとして、実務におけるモダンなシステムアーキテクチャ設計、AWSを用いた堅牢な本番インフラ構築、主要なセキュリティ脆弱性（アンチパターン）の排除、およびCI/CDによる品質管理を一貫して単独で実証するためのポートフォリオです。

単にプログラムが動くだけのモックではなく、 **「独自ドメイン未取得という制約下におけるフロント/バックエンド双方の常時SSL（HTTPS）化」** や **「CDN（CloudFront）導入時に発生するCORS/OPTIONS通信のヘッダー消失トラブル」** など、実際の開発現場で直面する高度なインフラ課題を自力でトラブルシューティングし、完全開通させた商用クオリティのシステムです。

---

## 📸 画面イメージ ＆ 本番環境URL
<table>
  <tr>
    <td align="center" width="50%">
      <b>Vue 3 (Vuetify) バージョン</b><br/>
      <img src="./images/vue-screenshot.png" alt="Vue3 Dashboard" width="100%"/>
    </td>
    <td align="center" width="50%">
      <b>React 19 (MUI) バージョン</b><br/>
      <img src="./images/react-screenshot.png" alt="React Dashboard" width="100%"/>
    </td>
  </tr>
</table>

- **本番環境（AWS / フルクラウド公開中）**: `https://d2td4ep83ibsl5.cloudfront.net`
- **本番環境（GCP / フルクラウド公開中）**: `http://8.231.131.233`

---

## 🗺️ マルチクラウド（AWS / GCP）本番インフラアーキテクチャ構成図

本プロジェクトでは、実務におけるマルチクラウド運用の実証として、AWS環境とGCP環境の2パターンの本番インフラを構築・検証しています。それぞれのインフラの制約とメリットを考慮した最適なアーキテクチャ設計を行っています。

### 【パターンA】AWS：マルチCDN常時SSL＆レイヤー完全分離構成（Vue 3版）
```mermaid
graph TD
    %% スタイル定義
    classDef internet fill:#f9f9f9,stroke:#333,stroke-width:2px;
    classDef aws fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    classDef public fill:#fff3e0,stroke:#f57c00,stroke-width:1px;
    classDef private fill:#efebe9,stroke:#5d4037,stroke-width:1px;

    User([ユーザー / ブラウザ]):::internet
    
    subgraph CloudFront_Layer [CloudFront / CDN レイヤー]
        CF1[CloudFront ① フロント用<br>HTTPS強制リダイレクト]
        CF2[CloudFront ② バックエンド用<br>CachingDisabled / AllViewerExceptHostHeader]
    end

    subgraph VPC [AWS VPC 仮想ネットワーク]
        subgraph Public_Subnet [パブリック・サブネット]
            S3[Amazon S3<br>Vue 3 静的ホスティング]
            EC2[Amazon EC2<br>Ubuntu Linux]
            Nginx[Nginx<br>リバースプロキシ Port: 80 -> 8080]
        end

        subgraph Private_Subnet [プライベート・サブネット]
            RDS[(Amazon RDS<br>PostgreSQL 16)]
        end
    end

    %% 通信経路
    User -->|① 静的コンテンツ配信| CF1
    User -->|② APIリクエスト| CF2
    
    CF1 --> S3
    CF2 -->|HTTPS Port: 443| Nginx
    Nginx -->|内部HTTP Port: 8080| EC2
    EC2 -->|内部DB通信 Port: 5432| RDS

    class VPC,Public_Subnet,Private_Subnet aws;
```

### 【パターンB】GCP：永年無料枠最大活用・CORS完全消滅構成（React 19版）
```mermaid
graph TD
    %% スタイル定義
    classDef internet fill:#f9f9f9,stroke:#333,stroke-width:2px;
    classDef gcp fill:#e8f5e9,stroke:#4caf50,stroke-width:2px;
    classDef vm fill:#ffffff,stroke:#333,stroke-width:1px;

    User([ユーザー / ブラウザ]):::internet

    subgraph GCE [GCP Compute Engine / e2-micro 永年無料インスタンス]
        Nginx[Nginx Webサーバー / リバースプロキシ<br>Port: 80 単一窓口]
        
        subgraph Same_Origin [同一オリジン / 内部空間]
            React[React 19 静的ファイル配信<br>/ 階層]
            Java[Java / Spring Boot API Server<br>Port: 8080 /api/ 階層]
            Docker[(Docker / PostgreSQL 16 コンテナ<br>データはVolume永続化)]
        end
    end

    %% 通信経路
    User -->|すべてのアクセスを受信 Port: 80| Nginx
    
    Nginx -->|① フロント配信| React
    Nginx -->|② 同一オリジン内部転送| Java
    Java -->|ローカル通信 Port: 5432| Docker

    class GCE gcp;
    class Same_Origin vm;
```

---

## 🛠️ 技術スタック

### インフラ / クラウド（マルチクラウド実証）
- **AWS環境**
  - **Amazon CloudFront**: フロント/バック双方の前段に配置。常時SSL化およびAPIキャッシュ無効化（`CachingDisabled`）の制御。
  - **Amazon S3**: フロントエンド静的ファイルのセキュアなホスティング。
  - **Amazon EC2 (Ubuntu)**: Javaアプリケーションサーバー。
  - **Nginx**: リバースプロキシとして導入。CloudFrontからの通信（ポート80）を受信し、Java（ポート8080）へ安全に仲介。
  - **Amazon VPC**: サブネット隔離、セキュリティグループによるIP直指定を排除した「グループ間連携ファイアウォール」の設計。
- **GCP環境**
  - **Google Compute Engine (GCE / e2-micro)**: 永年無料枠（Always Free）のVMインスタンス。Ubuntu 22.04 LTS環境を構築し、システム全体のホストサーバーとして運用。
  - **Docker / Docker Volume**: マネージドDBの課金を完全回避するための0円コンテナ運用。PostgreSQL 16コンテナを起立させ、データ永続化用の独立ボリューム（`postgres_data`）をバインドマウント。
  - **Nginx (GCP Web Server / Reverse Proxy)**: ポート80で全アクセスを受信する「単一窓口」として機能。同一サーバー内でReactの静的配信とJava（ポート8080）への内部転送を一括制御し、CORSの概念を根本から消滅させるリバースプロキシを設計。
  - **VPC ネットワーク (GCP Network)**: 静的外部IPアドレスの割り当て、およびブラウザSSHやHTTP通信に必要なインバウンドポート（22/80/8080）のみを厳格に制限するファイアウォールルールの設計。

### フロントエンド（マルチフロントエンド接続実証）
- **Vue 3** (Composition API, `<script setup>` 構文) / **Vuetify 3** ➔ AWS環境へ配備
- **React 19** (TypeScript / Functional Component / Hooks) / **MUI (Material-UI)** ➔ GCP環境へ配備
- **Axios**: 非同期HTTP通信（GCP環境ではCORSを回避するため相対パス `/api/tasks` で運用）

### バックエンド / DB
- **Java 21** (LTS 最新仕様)
- **Spring Boot 3.x**: (Spring Web, Spring Data JPA, Jakarta Validation, Spring AOP)
- **PostgreSQL 16**: (AWS RDS および GCP内 Dockerコンテナの双方で検証)

### テスト & 信頼性 (CI/CD)
- **JUnit 5** / **Mockito**: サービス層のビジネスロジックに対する単体テスト自動化。
- **GitHub Actions**: リポジトリへの `push` / `PR` 時の自動ビルド・自動テスト環境の構築。

---

## 🎯 設計のこだわり ＆ 実務アピールポイント

### 1. 独自ドメイン不要のマルチCDN常時SSL（HTTPS）化（★AWSアピール）
本番Webシステムにおいて必須となる「常時SSL化」を、独自ドメインを購入・割当することなくAWS標準機能のみで実現。
HTTPS環境からHTTP環境へ通信する際にブラウザが通信を強制遮断する「Mixed Content（ミックスコンテンツ）」の脆弱性に対し、EC2内に **Nginx（リバースプロキシ）** を構築し、バックエンド専用の2枚目のCloudFrontをプロキシとして前段に配置することで解決しました。
また、フロントエンドのデプロイ（仕様変更）時に発生する古い静的ファイルの残存リスクに対し、CloudFrontの **キャッシュ無効化（Invalidation: `/*`）** を用いたCDN運用保守フェーズのトラブルシューティングまでを実証しています。

### 2. API専用のCloudFront詳細ポリシーツーリング（CORS対策）
CloudFrontをAPIサーバーの前段に置いた際に発生する「CORS通信エラー」をインフラ設定レベルで根本解決。
- **メソッド拡張**: ブラウザが安全確認のために自動送信する「OPTIONS（プリフライト）リクエスト」や、`PUT`/`DELETE`メソッドがCDNで弾かれないよう、許可されたHTTPメソッドをフル開放しOPTIONSのキャッシュも有効化。
- **ヘッダー中継（`AllViewerExceptHostHeader`）**: 
  安易な `AllViewer` 設定（アンチパターン）によって発生する「Hostヘッダーの書き換えに伴うNginxのタイムアウト（504 Gateway Timeout）」を回避。CORS判定に必要な `Origin` 等のViewerヘッダーのみを無傷でJavaへ透過させ、HostヘッダーはAWS内部の正しい宛先に書き換える適正なポリシーを設計。

### 3. アプリケーション層での正攻法CORSガード
Java（Spring Boot）側のコントローラー層（`TaskController.java`）において、開発初期に使われがちな `origins = "*"` というワイルドカードによる全開放（アンチパターン）を徹底排除。本番環境であるフロントエンド用CloudFrontのドメインのみを明示的に指定した**ホワイトリスト方式（正攻法）**を採用し、CSRF（クロスサイトリクエストフォージェリ）等の脆弱性を防いでいます。

### 4. インフラコストを0円に抑える「GCP永年無料枠×Docker同居」設計（★GCPアピール）
GCPのマネージドDB（Cloud SQL）に無料枠が存在しない制約に対し、Compute Engine（e2-micro）の永年無料枠（Always Free）のディスク空間（30GB）を活用。VM内部に **DockerコンテナでPostgreSQL 16を起立** させ、ボリューム（`postgres_data`）をバインドマウントすることで、完全無料でデータを安全に保持（永続化）するコスト最適化アーキテクチャを実証しました。また、OS（Ubuntu 22.04 jammy）へのDocker公式リポジトリの導入から、環境に合わせたビルド・デプロイまでをコンソールから一気通貫で完結させています。

### 5. Nginxリバースプロキシによる「CORS（クロスオリジン）の根本消滅」
GCP環境（React版）においては、Nginxを全通信のフロントドア（単一窓口）として配置。
`/` へのアクセスでReactのビルド静的ファイルを配信し、`/api/` へのアクセスを同じサーバー内で待ち受けるJava（8080）へ内部転送する設計を採用しました。
ブラウザから見ると「画面の読み込み先」と「APIの叩き先」が完全に同一のIPアドレス・同一ポート（ポート80）の**「同一オリジン（Same-Origin）」**になるため、アプリケーション層での複雑なCORS許可設定やプリフライト（OPTIONS）通信のオーバーヘッドそのものを根本から100%排除した、堅牢でクリーンなネットワークラインを構築しています。

### 6. フロントエンド：スマート・ダムコンポーネント設計とUI/UX最適化
画面の肥大化・結合度を抑えるため、状態管理を行う「親（スマート）」とUI表示に特化した「子（ダム）」の役割を明確に分離。
- **リアルタイム検索・絞り込み＆高速フロントサイド・ソート**:
  ユーザーの利用規模（タスク増大）を想定し、一覧カード内に検索窓と優先度フィルターを統合。無駄なネットワーク通信（APIの再リクエスト）を発生させず、Vue 3の **`computed`（算出プロパティ）** やReactの **`sort()` ロジック** を用いて、ブラウザのメモリ空間上だけで一瞬で並び替え・絞り込みが完結する、サーバー負荷軽減とUX最大化を両立した設計を徹底。

### 7. バックエンド：Java最新仕様の網羅とWebアンチパターン対策
- **Record型の全面採用**: DTOに `Record` を採用し、データの不変性（Immutable）の担保と可読性向上を両立。
- **Stream API / ラムダ式 / Optional**: 泥臭いfor文（手続き型）を全廃し、可読性が高くバグの温床にならない「宣言的プログラミング」を徹底。
- **生SQL結合の排除**: Spring Data JPAによる自動プレースホルダー（バインド変数）ベースのクエリ発行に委ね、SQLインジェクション脆弱性を根本からシャットアウト。
- **画面フリーズ・生エラー露出の防止**: `@RestControllerAdvice` によるグローバル例外ハンドリングを構築。予期せぬエラー発生時もスタックトレースを隠蔽した一貫性のあるJSONをフロントに返却。
- **Spring AOPによる横断関心事の分離**: `@Aspect` を用いて、全APIの開始・終了、および例外発生時におけるパフォーマンスログおよびエラーログの出力を一括制御。

### 8. 品質管理の自動化 (JUnit 5 ＆ GitHub Actions)
- `TaskService` のビジネスロジックに対し、正常系・異常系を網羅したテストコードを JUnit 5 で記述。設定ファイル（`application.properties`）をGit管理から安全に除外しつつ、GitHub ActionsのCIビルド環境へ環境変数を安全に注入（`env:`）することで、常に自動テスト（CI）をパスした品質の高いコードのみがメインブランチにマージされる実務リリースフローを完全再現。
