package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.*
import com.github.unhappychoice.circleci.v1.response.*
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class MockCircleCIAPIClientV1_1: CircleCIAPIClientV1_1 {
    override fun getMe(): Observable<User> {
        return legacyFixtureResponse("json/me.json", User::class.java)
    }

    override fun getProjects(): Observable<List<Project>> {
        return legacyFixtureResponse("json/projects.json", object: TypeToken<List<Project>>(){}.type)
    }

    override fun getProjectBuilds(vcsType: String, userName: String, project: String, offset: Int, limit: Int): Observable<List<Build>> {
        return legacyFixtureResponse("json/builds.json", object: TypeToken<List<Build>>(){}.type)
    }

    override fun getBranchBuilds(vcsType: String, userName: String, project: String, branch: String, offset: Int, limit: Int): Observable<List<Build>> {
        return legacyFixtureResponse("json/builds.json", object: TypeToken<List<Build>>(){}.type)
    }

    override fun getRecentBuilds(offset: Int, limit: Int): Observable<List<Build>> {
        return legacyFixtureResponse("json/builds.json", object: TypeToken<List<Build>>(){}.type)
    }

    override fun getBuild(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<Build> {
        return legacyFixtureResponse("json/build.json", Build::class.java)
    }

    override fun getArtifacts(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<List<Artifact>> {
        return legacyFixtureResponse("json/artifacts.json", object: TypeToken<List<Artifact>>(){}.type)
    }

    override fun followProject(vcsType: String, userName: String, project: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun retryBuild(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<Build> {
        return legacyFixtureResponse("json/build.json", Build::class.java)
    }

    override fun cancelBuild(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<Build> {
        return legacyFixtureResponse("json/build.json", Build::class.java)
    }

    override fun addSSHUser(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<Build> {
        return legacyFixtureResponse("json/build.json", Build::class.java)
    }

    override fun triggerNewBuild(vcsType: String, userName: String, project: String, request: TriggerNewBuildRequest): Observable<Build> {
        return legacyFixtureResponse("json/build.json", Build::class.java)
    }

    override fun triggerNewBuildWithBranch(vcsType: String, userName: String, project: String, branch: String, request: TriggerNewBuildWithBranchRequest): Observable<Build> {
        return legacyFixtureResponse("json/build.json", Build::class.java)
    }

    override fun deleteCache(vcsType: String, userName: String, project: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun addSshKey(vcsType: String, userName: String, project: String, request: AddSshKeyRequest): Completable {
        return Completable.complete()
    }

    override fun getCheckoutKeys(vcsType: String, userName: String, project: String): Observable<List<CheckoutKey>> {
        return Observable.just(listOf(createMockCheckoutKey()))
    }

    override fun createCheckoutKey(vcsType: String, userName: String, project: String, request: CreateCheckoutKeyRequest): Observable<CheckoutKey> {
        return Observable.just(createMockCheckoutKey())
    }

    override fun getCheckoutKey(vcsType: String, userName: String, project: String, fingerprint: String): Observable<CheckoutKey> {
        return Observable.just(createMockCheckoutKey())
    }

    private fun createMockCheckoutKey() = CheckoutKey(
        type = "deploy-key",
        preferred = true,
        created_at = "2024-07-11T12:00:00Z",
        fingerprint = "xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx",
        public_key = "ssh-rsa AAAA..."
    )

    override fun deleteCheckoutKey(vcsType: String, userName: String, project: String, fingerprint: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun addUserSshKey(request: AddSshKeyRequest): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun addHerokuKey(apikey: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun getTestMetadata(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<TestMetadataResponse> {
        return Observable.just(TestMetadataResponse(
            tests = listOf(
                TestMetadata(
                    message = "",
                    file = "spec/example_spec.rb",
                    source = "rspec",
                    run_time = 0.123,
                    result = "success",
                    name = "Example test",
                    classname = "ExampleSpec"
                )
            )
        ))
    }

    override fun getEnvironmentVariables(vcsType: String, userName: String, project: String): Observable<List<EnvironmentVariable>> {
        return Observable.just(listOf(
            EnvironmentVariable(name = "MY_VAR", value = "xxxx1234")
        ))
    }

    override fun getEnvironmentVariable(vcsType: String, userName: String, project: String, name: String): Observable<EnvironmentVariable> {
        return Observable.just(EnvironmentVariable(name = name, value = "xxxx"))
    }

    override fun getLatestArtifacts(vcsType: String, userName: String, project: String, branch: String?, filter: String?): Observable<List<Artifact>> {
        return legacyFixtureResponse("json/artifacts.json", object: TypeToken<List<Artifact>>(){}.type)
    }

    override fun triggerPipeline(vcsType: String, userName: String, project: String, request: TriggerPipelineRequest): Observable<TriggerPipelineResponse> {
        return Observable.just(TriggerPipelineResponse(status = 200, body = "Build created"))
    }

    override fun addEnvironmentVariable(vcsType: String, userName: String, project: String, request: AddEnvironmentVariableRequest): Observable<EnvironmentVariable> {
        return Observable.just(EnvironmentVariable(name = request.name, value = "xxxx"))
    }

    override fun deleteSshKey(vcsType: String, userName: String, project: String, request: DeleteSshKeyRequest): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun deleteEnvironmentVariable(vcsType: String, userName: String, project: String, name: String): Observable<Unit> {
        return Observable.just(Unit)
    }
}
