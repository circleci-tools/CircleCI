package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.request.AddSshKeyRequest
import com.github.unhappychoice.circleci.v1.request.CreateCheckoutKeyRequest
import com.github.unhappychoice.circleci.v1.request.TriggerNewBuildRequest
import com.github.unhappychoice.circleci.v1.request.TriggerNewBuildWithBranchRequest
import com.github.unhappychoice.circleci.v1.response.*
import com.github.unhappychoice.circleci.v2.response.CheckoutKey
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import java.io.File
import java.lang.reflect.Type
import java.util.*

class MockCircleCIAPIClientV1_1: CircleCIAPIClientV1_1 {
    override fun getMe(): Observable<User> {
        return mockResponse("json/me.json", User::class.java)
    }

    override fun getProjects(): Observable<List<Project>> {
        return mockResponse("json/projects.json", object: TypeToken<List<Project>>(){}.type)
    }

    override fun getProjectBuilds(vcsType: String, userName: String, project: String, offset: Int, limit: Int): Observable<List<Build>> {
        return mockResponse("json/builds.json", object: TypeToken<List<Build>>(){}.type)
    }

    override fun getBranchBuilds(vcsType: String, userName: String, project: String, branch: String, offset: Int, limit: Int): Observable<List<Build>> {
        return mockResponse("json/builds.json", object: TypeToken<List<Build>>(){}.type)
    }

    override fun getRecentBuilds(offset: Int, limit: Int): Observable<List<Build>> {
        return mockResponse("json/builds.json", object: TypeToken<List<Build>>(){}.type)
    }

    override fun getBuild(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<Build> {
        return mockResponse("json/build.json", Build::class.java)
    }

    override fun getArtifacts(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<List<Artifact>> {
        return mockResponse("json/artifacts.json", object: TypeToken<List<Artifact>>(){}.type)
    }

    override fun followProject(vcsType: String, userName: String, project: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun retryBuild(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<Build> {
        return mockResponse("json/build.json", Build::class.java)
    }

    override fun cancelBuild(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<Build> {
        return mockResponse("json/build.json", Build::class.java)
    }

    override fun addSSHUser(vcsType: String, userName: String, project: String, buildNumber: Int): Observable<Build> {
        return mockResponse("json/build.json", Build::class.java)
    }

    override fun triggerNewBuild(vcsType: String, userName: String, project: String, request: TriggerNewBuildRequest): Observable<Build> {
        return mockResponse("json/build.json", Build::class.java)
    }

    override fun triggerNewBuildWithBranch(vcsType: String, userName: String, project: String, branch: String, request: TriggerNewBuildWithBranchRequest): Observable<Build> {
        return mockResponse("json/build.json", Build::class.java)
    }

    override fun deleteCache(vcsType: String, userName: String, project: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun addSshKey(vcsType: String, userName: String, project: String, request: AddSshKeyRequest): Observable<SSHKey> {
        return mockResponse("json/ssh_key.json", SSHKey::class.java)
    }

    override fun getCheckoutKeys(vcsType: String, userName: String, project: String): Observable<List<CheckoutKey>> {
        return mockResponse("json/checkout_keys.json", object: TypeToken<List<CheckoutKey>>(){}.type)
    }

    override fun createCheckoutKey(vcsType: String, userName: String, project: String, request: CreateCheckoutKeyRequest): Observable<CheckoutKey> {
        return mockResponse("json/checkout_key.json", CheckoutKey::class.java)
    }

    override fun getCheckoutKey(vcsType: String, userName: String, project: String, fingerprint: String): Observable<CheckoutKey> {
        return mockResponse("json/checkout_key.json", CheckoutKey::class.java)
    }

    override fun deleteCheckoutKey(vcsType: String, userName: String, project: String, fingerprint: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun addUserSshKey(request: AddSshKeyRequest): Observable<Unit> {
        return Observable.just(Unit)
    }

    override fun addHerokuKey(apikey: String): Observable<Unit> {
        return Observable.just(Unit)
    }

    private fun <T> mockResponse(fileName: String, type: Type): Observable<T> {
        return Observable.just(converter().fromJson(readJson(fileName), type))
    }

    private fun converter() = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    private fun readJson(fileName: String): String {
        val file = File(MockCircleCIAPIClientV1::class.java.getResource(fileName).file)
        val scanner = Scanner(file)
        try {
            return scanner.useDelimiter("\\A").next()
        } finally {
            scanner.close()
        }
    }
}
